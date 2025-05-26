public class Vector {
    public float x;
    public float y;

    private static final float EPS = 1e-6f;

    public Vector() {
        this(0f, 0f);
    }

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector copy() {
        return new Vector(x, y);
    }

    public float length() {
        return (float) Math.hypot(x, y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public Vector normalize() {
        float len = length();
        if (len > EPS) {
            x /= len;
            y /= len;
        }
        return this;
    }

    public Vector add(Vector o) {
        x += o.x;
        y += o.y;
        return this;
    }

    public Vector add(float ox, float oy) {
        x += ox;
        y += oy;
        return this;
    }

    public Vector sub(Vector o) {
        x -= o.x;
        y -= o.y;
        return this;
    }

    public Vector scale(float s) {
        x *= s;
        y *= s;
        return this;
    }

    public float dot(Vector o) {
        return x * o.x + y * o.y;
    }

    public float cross(Vector o) {
        return x * o.y - y * o.x;
    }

    public Vector lerp(Vector o, float t) {
        float t1 = 1f - t;
        x = x * t1 + o.x * t;
        y = y * t1 + o.y * t;
        return this;
    }

    public Vector limit(float max) {
        float len = length();
        if (len > max && len > EPS) {
            float ratio = max / len;
            x *= ratio;
            y *= ratio;
        }
        return this;
    }

    public Vector rotate(float radians) {
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);
        float nx = x * cos - y * sin;
        float ny = x * sin + y * cos;
        x = nx;
        y = ny;
        return this;
    }

    public float angle() {
        return (float)Math.atan2(y, x);
    }

    public float distance(Vector o) {
        float dx = x - o.x;
        float dy = y - o.y;
        return (float)Math.hypot(dx, dy);
    }

    public float distanceSquared(Vector o) {
        float dx = x - o.x;
        float dy = y - o.y;
        return dx * dx + dy * dy;
    }

    @Override
    public String toString() {
        return "Vector(" + x + ", " + y + ")";
    }
}
