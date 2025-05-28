public class Vector {
    // X component of the vector
    public float x;

    // Y component of the vector
    public float y;

    // Epsilon value for floating-point comparisons to avoid precision issues
    private static final float EPS = 1e-6f;

    // Default constructor initializes the vector to (0, 0)
    public Vector() {
        this(0f, 0f);
    }

    // Constructor to initialize the vector with specific x and y components
    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Sets the x and y components of the vector and returns the updated vector
    public Vector set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    // Creates and returns a copy of the current vector
    public Vector copy() {
        return new Vector(x, y);
    }

    // Calculates and returns the length (magnitude) of the vector
    public float length() {
        return (float) Math.hypot(x, y);
    }

    // Calculates and returns the squared length of the vector
    public float lengthSquared() {
        return x * x + y * y;
    }

    // Normalizes the vector (makes its length equal to 1) if its length is greater
    // than EPS
    public Vector normalize() {
        float len = length();
        if (len > EPS) {
            x /= len;
            y /= len;
        }
        return this;
    }

    // Adds another vector to this vector and returns the updated vector
    public Vector add(Vector o) {
        x += o.x;
        y += o.y;
        return this;
    }

    // Adds specific x and y values to this vector and returns the updated vector
    public Vector add(float ox, float oy) {
        x += ox;
        y += oy;
        return this;
    }

    // Subtracts another vector from this vector and returns the updated vector
    public Vector sub(Vector o) {
        x -= o.x;
        y -= o.y;
        return this;
    }

    // Scales the vector by a scalar value and returns the updated vector
    public Vector scale(float s) {
        x *= s;
        y *= s;
        return this;
    }

    // Calculates and returns the dot product of this vector with another vector
    public float dot(Vector o) {
        return x * o.x + y * o.y;
    }

    // Calculates and returns the cross product of this vector with another vector
    public float cross(Vector o) {
        return x * o.y - y * o.x;
    }

    // Linearly interpolates between this vector and another vector by a factor t
    // and returns the updated vector
    public Vector lerp(Vector o, float t) {
        float t1 = 1f - t;
        x = x * t1 + o.x * t;
        y = y * t1 + o.y * t;
        return this;
    }

    // Limits the length of the vector to a maximum value and returns the updated
    // vector
    public Vector limit(float max) {
        float len = length();
        if (len > max && len > EPS) {
            float ratio = max / len;
            x *= ratio;
            y *= ratio;
        }
        return this;
    }

    // Rotates the vector by a specified angle in radians and returns the updated
    // vector
    public Vector rotate(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float nx = x * cos - y * sin;
        float ny = x * sin + y * cos;
        x = nx;
        y = ny;
        return this;
    }

    // Calculates and returns the angle of the vector in radians relative to the
    // positive x-axis
    public float angle() {
        return (float) Math.atan2(y, x);
    }

    // Calculates and returns the distance between this vector and another vector
    public float distance(Vector o) {
        float dx = x - o.x;
        float dy = y - o.y;
        return (float) Math.hypot(dx, dy);
    }

    // Calculates and returns the squared distance between this vector and another
    // vector
    public float distanceSquared(Vector o) {
        float dx = x - o.x;
        float dy = y - o.y;
        return dx * dx + dy * dy;
    }

    // Returns a string representation of the vector in the format "Vector(x, y)"
    @Override
    public String toString() {
        return "Vector(" + x + ", " + y + ")";
    }
}
