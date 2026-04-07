package art.intel.soft.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import art.intel.soft.data.repository.AuthRepository
import art.intel.soft.session.UserSession
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AuthBottomSheet : BottomSheetDialogFragment() {

    var onAuthSuccess: (() -> Unit)? = null
    var onLogout: (() -> Unit)? = null
    private val disposables = CompositeDisposable()

    // Build layout programmatically (no XML needed)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val ctx = requireContext()

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 48)
        }

        if (UserSession.isAuthenticated) {
            val signedInTitle = TextView(ctx).apply {
                text = "Вы вошли в аккаунт"
                textSize = 18f
                setPadding(0, 0, 0, 24)
            }
            root.addView(signedInTitle)

            val logoutButton = Button(ctx).apply {
                text = "Выйти из аккаунта"
            }
            logoutButton.setOnClickListener {
                UserSession.logout()
                onLogout?.invoke()
                dismiss()
            }
            root.addView(logoutButton)

            return root
        }

        val title = TextView(ctx).apply {
            text = "Войдите, чтобы открыть все функции"
            textSize = 18f
            setPadding(0, 0, 0, 24)
        }
        root.addView(title)

        val tabs = TabLayout(ctx).apply {
            addTab(newTab().setText("Регистрация"))
            addTab(newTab().setText("Войти"))
        }
        root.addView(tabs)

        val emailInput = EditText(ctx).apply {
            hint = "Email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or android.text.InputType.TYPE_CLASS_TEXT
            setPadding(0, 16, 0, 8)
        }
        root.addView(emailInput)

        val passwordInput = EditText(ctx).apply {
            hint = "Пароль (минимум 8 символов)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(0, 8, 0, 24)
        }
        root.addView(passwordInput)

        val errorText = TextView(ctx).apply {
            setTextColor(0xFFE53935.toInt())
            textSize = 14f
            visibility = View.GONE
            setPadding(0, 0, 0, 8)
        }
        root.addView(errorText)

        val actionButton = Button(ctx).apply {
            text = "Зарегистрироваться"
        }
        root.addView(actionButton)

        var isLoginMode = false

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                isLoginMode = tab?.position == 1
                actionButton.text = if (isLoginMode) "Войти" else "Зарегистрироваться"
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        actionButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                errorText.text = "Заполните все поля"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorText.text = "Введите корректный email"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (password.length < 8) {
                errorText.text = "Пароль должен содержать минимум 8 символов"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            actionButton.isEnabled = false
            errorText.visibility = View.GONE

            val request = if (isLoginMode) {
                AuthRepository.login(email, password)
            } else {
                AuthRepository.register(email, password)
            }

            val disposable = request
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ _ ->
                    onAuthSuccess?.invoke()
                    dismiss()
                }, { error ->
                    actionButton.isEnabled = true
                    errorText.text = when {
                        error.message?.contains("400") == true -> "Email уже зарегистрирован"
                        error.message?.contains("401") == true -> "Неверный email или пароль"
                        error.message?.contains("422") == true -> "Введите корректный email"
                        else -> "Ошибка сети. Проверьте подключение"
                    }
                    errorText.visibility = View.VISIBLE
                })
            disposables.add(disposable)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    companion object {
        const val TAG = "AuthBottomSheet"
        fun newInstance(): AuthBottomSheet = AuthBottomSheet()
    }
}
