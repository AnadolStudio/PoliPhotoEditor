package art.intel.soft.utils.bitmaputils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Single;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static android.graphics.Shader.TileMode.REPEAT;

public class CutUtils {
    public static final int SIDE_OF_SQUARE = 60;
    private static final int DEFAULT_RADIUS = 1;
    private static final int RADIUS_BLUR_DEFAULT = 12;
    private static final int DEFAULT_DEEP = 5;

    public static int[] erosion(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == 0 || h == 0) {
            throw new IllegalArgumentException("Weight or height must be more than 0");
        }

        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        return erosion(pixels, w, h);
    }

    public static int[] erosion(int[] pixels, int width, int height) {
        Set<Integer> delete = new HashSet<>();

        for (int i = 0; i < height * width; i++) {
            int pixel = pixels[i];
            if (pixel != Color.TRANSPARENT && hasTransparentNeighbors(i, DEFAULT_RADIUS, pixels, width, height)) {
                delete.add(i);
            }
        }
        for (int i :
                delete) {
            pixels[i] = Color.TRANSPARENT;
        }
        return pixels;
    }

    public static int[] opening(int[] pixels, int width, int height, int color) {
        return dilation(
                erosion(pixels, width, height),
                width,
                height, color);
    }

    public static int[] opening(Bitmap bitmap, int color) {
        return dilation(
                erosion(bitmap),
                bitmap.getWidth(),
                bitmap.getHeight(), color);
    }

    public static int[] closing(int[] pixels, int width, int height, int color) {
        return erosion(
                dilation(pixels, width, height, color),
                width,
                height);
    }

    public static int[] closing(Bitmap bitmap, int color) {
        return erosion(
                dilation(bitmap, color),
                bitmap.getWidth(),
                bitmap.getHeight());
    }

    public static int[] dilation(Bitmap bitmap, int color) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == 0 || h == 0) {
            throw new IllegalArgumentException("Weight or height must be more than 0");
        }

        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        return dilation(pixels, w, h, color);
    }

    public static int[] dilation(int[] pixels, int width, int height, int color) {
        Set<Integer> add = new HashSet<>();

        for (int i = 0; i < height * width; i++) {
            int pixel = pixels[i];
            if (pixel != Color.TRANSPARENT) {
                for (int neighbour : getNeighbors(i, DEFAULT_RADIUS, width, height)) {
                    if (neighbour == Color.TRANSPARENT) {
                        add.add(neighbour);
                    }
                }
            }
        }
        for (int i : add) {
            pixels[i] = color;
        }
        return pixels;
    }

    public static HashMap<Integer, Integer> getEdgePixels(Bitmap bitmap, int deep) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == 0 || h == 0) {
            throw new IllegalArgumentException("Weight or height must be more than 0");
        }

        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        return getEdgePixels(pixels, w, h, deep);
    }

    public static HashMap<Integer, Integer> getEdgePixels(int[] pixels, int w, int h, int deep) {
        HashMap<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < h * w; i++) {
            if (pixels[i] != Color.TRANSPARENT && hasTransparentNeighbors(i, 1, pixels, w, h)) {
                for (int j : getCircleNeighbors(i, deep, w, h)) {
                    if (pixels[j] == Color.TRANSPARENT) {
                        continue;
                    }

                    int r = getRadius(i, j, w);

                    if (map.containsKey(j)) {
                        map.put(j, Math.min(r, map.get(j)));
                    } else {
                        map.put(j, r);
                    }
                }
            }
        }
        return map;
    }

    public static ArrayList<Integer> getEdgePixelsList(int[] pixels, int w, int h, int deep) {
        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < h * w; i++) {
            if (pixels[i] != Color.TRANSPARENT && hasTransparentNeighbors(i, 1, pixels, w, h)) {
                for (int j : getCircleNeighbors(i, deep, w, h)) {
                    if (pixels[j] == Color.TRANSPARENT) {
                        continue;
                    }
                    result.add(j);
                }
            }
        }
        return result;
    }

    private static boolean hasTransparentNeighbors(int index, int radius, int[] pixels, int width, int height) {
        ArrayList<Integer> neighbors = getNeighbors(index, radius, width, height);
        for (int i : neighbors) {
            if (pixels[i] == Color.TRANSPARENT) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Integer> getNeighbors(int center, int radius, int width, int height) {
        ArrayList<Integer> result = new ArrayList<>();
        if (center < 0) {
            return result;
        }

        Cell top = Cell.Companion.toCell(center, width);
        Cell left = Cell.Companion.toCell(center, width);
        Cell bottom = Cell.Companion.toCell(center, width);
        Cell right = Cell.Companion.toCell(center, width);

        Cell centerCell = Cell.Companion.toCell(center, width);

        for (int i = 1; i <= radius; i++) {
            //Поиск верхней границы
            Cell current = new Cell(centerCell.getRow() - i, centerCell.getColumn());
            if (current.getRow() > 0) {
                top = current;
            }

            //Поиск левой границы
            current = new Cell(centerCell.getRow(), centerCell.getColumn() - i);
            if (current.getColumn() > 0) {
                left = current;
            }

            //Поиск нижней границы
            current = new Cell(centerCell.getRow() + i, centerCell.getColumn());
            if (current.getRow() < height - 1) {
                bottom = current;
            }

            //Поиск правой границы
            current = new Cell(centerCell.getRow(), centerCell.getColumn() + i);
            if (current.getColumn() < width - 1) {
                right = current;
            }
        }


        for (int row = top.getRow(); row <= bottom.getRow(); row++) {
            for (int column = left.getColumn(); column <= right.getColumn(); column++) {
                result.add(Cell.Companion.toIndex(row, column, width));
            }
        }
        return result;
    }

    public static ArrayList<Integer> getCircleNeighbors(int center, int radius, int width, int height) {
        ArrayList<Integer> neighbors = getNeighbors(center, radius, width, height);
        ArrayList<Integer> result = new ArrayList<>();
        int side = (int) Math.sqrt(neighbors.size() + 1);

        Cell LT = new Cell(0, 0);
        Cell RT = new Cell(0, side - 1);
        Cell LB = new Cell(side - 1, 0);
        Cell RB = new Cell(side - 1, side - 1);

        ArrayList<Integer> doNotUse = new ArrayList<>();
        if (radius >= 3) {// 3
            doNotUse.add(LT.toIndex(side));
            doNotUse.add(RT.toIndex(side));
            doNotUse.add(LB.toIndex(side));
            doNotUse.add(RB.toIndex(side));
        }

        if (radius >= 7) {
            doNotUse.add(LT.toIndex(side) + 1);
            doNotUse.add(LT.toIndex(side) + side);

            doNotUse.add(RT.toIndex(side) - 1);
            doNotUse.add(RT.toIndex(side) + side);

            doNotUse.add(LB.toIndex(side) + 1);
            doNotUse.add(LB.toIndex(side) - side);

            doNotUse.add(RB.toIndex(side) - 1);
            doNotUse.add(RB.toIndex(side) - side);
        }
        for (int i = 0; i < neighbors.size(); i++) {
            if (doNotUse.contains(i)) {
                continue;
            }
            result.add(neighbors.get(i));
        }

        return result;
    }

    public static int getRadius(int indexCenter, int indexPoint, int width) {
        if (indexCenter == indexPoint) {
            return 0;
        }
        Cell cellCenter = Cell.Companion.toCell(indexCenter, width);
        Cell cellPoint = Cell.Companion.toCell(indexPoint, width);
        int cathetOne = Math.abs(cellCenter.getColumn() - cellPoint.getColumn());
        int cathetTwo = Math.abs(cellCenter.getRow() - cellPoint.getRow());
        return cathetOne + cathetTwo;
    }

    public static Bitmap getBitmapFromColor(int color, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);
        return bitmap;
    }

    public static Bitmap createNullBackground(Point point) {
        Bitmap background = createNullSquare(SIDE_OF_SQUARE);

        Bitmap bitmap = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(background, REPEAT, REPEAT));
        canvas.drawRect(new Rect(0, 0, point.x, point.y), paint);
        return bitmap;
    }

    public static Bitmap createNullSquare(int side) {
        Bitmap background = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        int x = side / 2;
        canvas.drawRect(new Rect(0, 0, x, x), paint);
        canvas.drawRect(new Rect(x, x, x * 2, x * 2), paint);

        paint.setColor(Color.LTGRAY);
        canvas.drawRect(new Rect(0, x, x, x * 2), paint);
        canvas.drawRect(new Rect(x, 0, x * 2, x), paint);

        return background;
    }

    public static int getColorWithAlpha(int color, int alpha) {
        return Color.argb(alpha, red(color), green(color), blue(color));
    }

    @Deprecated
    public static Bitmap blur(Context context, int[] main, int w, int h, int radius) {
        Bitmap mainBitmap = Bitmap.createBitmap(main, w, h, Bitmap.Config.ARGB_8888);

        return BitmapEffectsKt.blur(context, mainBitmap, radius);
    }

    @Deprecated
    public static Bitmap blur(Context context, int[] main, int[] edge, int w, int h, int radius) {
        Bitmap edgeBitmap = Bitmap.createBitmap(edge, w, h, Bitmap.Config.ARGB_8888);
        Bitmap blur = BitmapEffectsKt.blur(context, edgeBitmap, radius);
        Bitmap mainBitmap = Bitmap.createBitmap(main, w, h, Bitmap.Config.ARGB_8888);

        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(blur, 0, 0, null);
        canvas.drawBitmap(mainBitmap, 0, 0, null);

        mainBitmap.recycle();
        blur.recycle();
        edgeBitmap.recycle();

        return result;
    }

    @Deprecated
    public static Single<Bitmap> cutBackground(Context context, Bitmap mainViewBitmap, Bitmap drawViewBitmap) {
        return Single.create(emitter -> {
            try {
                Bitmap bitmap = BitmapUtilKt.scaleBitmap(mainViewBitmap, drawViewBitmap);

                Point main = new Point(mainViewBitmap.getWidth(), mainViewBitmap.getHeight());
                Point support = new Point(bitmap.getWidth(), bitmap.getHeight());

                int[] pixelsOriginal = new int[main.x * main.y];
                int[] pixelsInverseOriginal = new int[main.x * main.y];
                int[] pixelsBitmap = new int[support.x * support.y];

                if (pixelsBitmap.length != pixelsOriginal.length) {
                    return;
                }

                mainViewBitmap.getPixels(pixelsOriginal, 0, main.x, 0, 0, main.x, main.y);
                bitmap.getPixels(pixelsBitmap, 0, support.x, 0, 0, support.x, support.y);

                HashMap<Integer, Integer> edgePixels = getEdgePixels(bitmap, DEFAULT_DEEP);

                for (int i = 0; i < pixelsOriginal.length; i++) {
                    if (pixelsBitmap[i] != Color.TRANSPARENT) {
                        pixelsInverseOriginal[i] = pixelsOriginal[i];
                        pixelsOriginal[i] = Color.TRANSPARENT;
                    }
                }

                int[] edgePixelsArray = pixelsOriginal.clone();

                for (int i : edgePixels.keySet()) {
                    pixelsOriginal[i] = Color.TRANSPARENT;
                }

                Bitmap result = blur(context, pixelsOriginal,
                        edgePixelsArray,
                        main.x, main.y,
                        RADIUS_BLUR_DEFAULT);
                result.getPixels(pixelsOriginal, 0, main.x, 0, 0, main.x, main.y);

                Bitmap inverseBlur = blur(context, pixelsInverseOriginal,
                        main.x, main.y,
                        RADIUS_BLUR_DEFAULT);
                inverseBlur.getPixels(pixelsInverseOriginal, 0, main.x, 0, 0, main.x, main.y);

                for (int i = 0; i < pixelsInverseOriginal.length; i++) {
                    int alpha = 255 - Color.alpha(pixelsInverseOriginal[i]);
                    pixelsOriginal[i] = getColorWithAlpha(pixelsOriginal[i], alpha);
                }

                emitter.onSuccess(Bitmap.createBitmap(pixelsOriginal, main.x, main.y, Bitmap.Config.ARGB_8888));
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
