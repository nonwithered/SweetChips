package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

public class UncheckcastPrepareAnnotationVisitor extends AnnotationVisitor {

    private static final String VALUE_STRING = "value";

    private final Consumer<Type> mConsumer;

    public UncheckcastPrepareAnnotationVisitor(int api, AnnotationVisitor av, Consumer<Type> consumer) {
        super(api, av);
        mConsumer = consumer;
    }
    @Override
    public AnnotationVisitor visitArray(String name) {
        if (name.equals(VALUE_STRING)) {
            return new AnnotationVisitor(api, super.visitArray(name)) {
                @Override
                public void visit(String name, Object value) {
                    if (name == null && value instanceof Type) {
                        mConsumer.accept((Type) value);
                    }
                    super.visit(name, value);
                }
            };
        }
        return super.visitArray(name);
    }
}
