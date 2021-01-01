package gov.nasa.worldwind.util.typescript;

public @interface TypeScript {

    boolean skipMethod() default false;

    String newMethodDecl() default "";

    String newClassDecl() default "";

    String newMethod() default "";
    
    boolean skipLines() default false;
    
    String substitute() default "";
}
