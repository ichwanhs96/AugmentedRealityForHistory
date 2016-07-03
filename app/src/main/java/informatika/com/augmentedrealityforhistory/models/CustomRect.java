package informatika.com.augmentedrealityforhistory.models;

import android.graphics.Rect;

/**
 * Created by Ichwan Haryo Sembodo on 30/06/2016.
 */
public class CustomRect {
    private Rect rect;
    private int x;
    private int y;
    private int objWidth;
    private int objHeight;

    public CustomRect(){
        this.x = 0;
        this.y = 0;
        this.objHeight = 0;
        this.objWidth = 0;
        rect = new Rect();
    }

    public CustomRect(int x, int y){
        this.x = x;
        this.y = y;
        this.objHeight = 0;
        this.objWidth = 0;
        rect = new Rect();
    }

    public void setRect(){
        this.rect.set(x, y, x+objWidth, y+objHeight);
    }

    public int getObjWidth() {
        return objWidth;
    }

    public void setObjWidth(int objWidth) {
        this.objWidth = objWidth;
    }

    public int getObjHeight() {
        return objHeight;
    }

    public void setObjHeight(int objHeight) {
        this.objHeight = objHeight;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
