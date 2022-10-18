package deck.wrapper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageWrapper extends BaseWrapper{

    private static final String TYPE_NAME = "FILE";

    public static ImageWrapper getImage(String path) {
        if (!checkSourcePermission(TYPE_NAME, path)) {
            throw new RuntimeException("file no permission");
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return new ImageWrapper(bitmap);
    }

    private final Bitmap bitmap;

    public ImageWrapper(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getPixel(int x, int y) {
        return this.bitmap.getPixel(x, y);
    }

    public int getWidth() {
        return this.bitmap.getWidth();
    }

    public int getHeight() {
        return this.bitmap.getHeight();
    }
}
