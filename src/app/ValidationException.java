package app;

import java.util.ArrayList;
import java.util.List;

public class ValidationException extends Exception {
    public final List<String> errors = new ArrayList<>();
}
