"""Unit tests for the feature_gates module."""
from app.feature_gates import (
    FEATURES,
    FILTERS,
    GUEST_FILTER_LIMIT,
    get_feature_access,
    get_filters_with_access,
)


class TestGetFeatureAccess:
    def test_guest_allowed_features_are_true(self):
        access = get_feature_access(is_authenticated=False)
        for key, cfg in FEATURES.items():
            if cfg["guest_allowed"]:
                assert access[key] is True, f"{key} should be True for guest"

    def test_guest_restricted_features_are_false(self):
        access = get_feature_access(is_authenticated=False)
        for key, cfg in FEATURES.items():
            if not cfg["guest_allowed"]:
                assert access[key] is False, f"{key} should be False for guest"

    def test_person_all_features_true(self):
        access = get_feature_access(is_authenticated=True)
        assert all(v is True for v in access.values()), (
            f"Person should have all features True, failed: "
            f"{[k for k, v in access.items() if not v]}"
        )

    def test_contains_all_19_features(self):
        access = get_feature_access(is_authenticated=False)
        assert len(access) == 19

    def test_specific_guest_allowed(self):
        access = get_feature_access(is_authenticated=False)
        for key in ["filters_basic", "crop_rotate_flip", "improve_basic",
                    "text_basic", "stickers_basic", "frames_basic",
                    "collage_basic", "forms_basic"]:
            assert access[key] is True

    def test_specific_guest_restricted(self):
        access = get_feature_access(is_authenticated=False)
        for key in ["filters_all", "curves", "effects", "fonts_extended",
                    "stickers_all", "frames_all", "collage_all", "forms_all",
                    "background_removal", "body_editor", "export_no_watermark"]:
            assert access[key] is False


class TestGetFiltersWithAccess:
    def test_total_filter_count(self):
        filters = get_filters_with_access(is_authenticated=False)
        assert len(filters) == 95

    def test_guest_first_20_available(self):
        filters = {f["id"]: f for f in get_filters_with_access(is_authenticated=False)}
        for i in range(GUEST_FILTER_LIMIT):
            assert filters[i]["available"] is True, f"Filter {i} should be available to guest"

    def test_guest_rest_unavailable(self):
        filters = {f["id"]: f for f in get_filters_with_access(is_authenticated=False)}
        for i in range(GUEST_FILTER_LIMIT, 95):
            assert filters[i]["available"] is False, f"Filter {i} should not be available to guest"

    def test_person_all_filters_available(self):
        filters = get_filters_with_access(is_authenticated=True)
        assert all(f["available"] is True for f in filters)

    def test_filter_has_required_fields(self):
        for f in get_filters_with_access(is_authenticated=False):
            assert "id" in f
            assert "name" in f
            assert "group" in f
            assert "available" in f

    def test_filter_ids_are_sequential(self):
        filters = get_filters_with_access(is_authenticated=False)
        ids = [f["id"] for f in filters]
        assert ids == list(range(95))

    def test_original_filter_always_available(self):
        filters = {f["id"]: f for f in get_filters_with_access(is_authenticated=False)}
        assert filters[0]["name"] == "Original"
        assert filters[0]["available"] is True
