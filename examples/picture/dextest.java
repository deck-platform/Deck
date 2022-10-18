import deck.wrapper.ContextWrapper;
import deck.wrapper.ImageWrapper;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class dextest {

    static class Result {
        private String greenPercentage;
        private String pictureName;

        public void setGreenPercentage(String greenPercentage) {
            this.greenPercentage = greenPercentage;
        }

        public void setPictureName(String pictureName) {
            this.pictureName = pictureName;
        }

        public String getGreenPercentage() {
            return this.greenPercentage;
        }

        public String getPictureName() {
            return this.pictureName;
        }

        public String toString() {
            return this.pictureName + ":" + this.greenPercentage;
        }
    }

    public static Result calculateGreen(File file) throws Exception {
        String path = file.getPath();
        ImageWrapper imageWrapper = ImageWrapper.getImage(path);
        int width = imageWrapper.getWidth();
        int height = imageWrapper.getHeight();
        long greenNumber = 0;
        int[] rgb = new int[3];
        float[] hsv = new float[3];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = imageWrapper.getPixel(i, j);
                rgb[0] = (pixel & 0xff0000) >> 16;
                rgb[1] = (pixel & 0xff00) >> 8;
                rgb[2] = (pixel & 0xff);
                RGBtoHSB(rgb[0], rgb[1], rgb[2], hsv);
                if (hsv[2] >= 0.075 && hsv[1] >= 0.15 && hsv[0] > 0.1389 &&
                        hsv[0] <= 0.4444) {
                    greenNumber++;
                }
            }
        }
        double greenPixelProportion = (double) greenNumber / (width * height);
        Result result = new Result();
        result.setPictureName(file.getName());
        result.setGreenPercentage(translateDoubleIntoPercent(greenPixelProportion));
        return result;
    }

    public static String translateDoubleIntoPercent(double d) {
        BigDecimal bDecimal = new BigDecimal(d);
        bDecimal = bDecimal.setScale(4, BigDecimal.ROUND_HALF_UP);
        DecimalFormat dFormat = new DecimalFormat("0.00%");
        return dFormat.format(bDecimal.doubleValue());
    }

    public String run(ContextWrapper contextWrapper) {
        File dir = new File("/data/data/com.bupt.deck/files");
        System.out.println(dir.getPath());
        System.out.println(Arrays.toString(dir.list()));
        int num = 2;
        List<String> result = new ArrayList<>();
        File[] fileList = dir.listFiles();
        if (null == fileList) {
            System.out.println("dir has not pictures");
            return result.toString();
        }
        int resultNum = Math.min(num, fileList.length);
        try {
            for (int index = 0; index < resultNum; index++) {
                System.out.println(fileList[index].getPath());
                result.add(calculateGreen(fileList[index]).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue, saturation, brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = ((float) cmax) / 255.0f;
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }
}
