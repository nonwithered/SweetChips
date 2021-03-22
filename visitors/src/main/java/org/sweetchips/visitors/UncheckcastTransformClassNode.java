package org.sweetchips.visitors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.sweetchips.annotations.Uncheckcast;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class UncheckcastTransformClassNode extends ClassNode {

    public UncheckcastTransformClassNode(int api) {
        super(api);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void accept(ClassVisitor cv) {
        Collection<Type> types = getTypes(invisibleAnnotations);
        for (MethodNode mtd : (List<MethodNode>) methods) {
            Collection<Type> collection = getTypes(mtd.invisibleAnnotations);
            ListIterator<AbstractInsnNode> iterator = mtd.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode abstractInsnNode = iterator.next();
                if (abstractInsnNode.getOpcode() == Opcodes.CHECKCAST) {
                    String desc = ((TypeInsnNode) abstractInsnNode).desc;
                    if (desc.charAt(0) != '[') {
                        desc = 'L' + desc + ';';
                    }
                    Type type = Type.getType(desc);
                    if (types == null || types.contains(type) || collection == null || collection.contains(type)) {
                        iterator.remove();
                    }
                }
            }
        }
        super.accept(cv);
    }

    private Collection<Type> getTypes(List<AnnotationNode> annotations) {
        if (annotations == null) {
            return Collections.emptySet();
        }
        Iterator<AnnotationNode> iterator = annotations.iterator();
        while (iterator.hasNext()) {
            AnnotationNode annotationNode = iterator.next();
            if (annotationNode.desc.equals("L" + Uncheckcast.class.getName().replaceAll("\\.", "/") + ";")) {
                iterator.remove();
                Collection<Type> types = new HashSet<>();
                annotationNode.accept(new AnnotationVisitor(api) {
                    @Override
                    public AnnotationVisitor visitArray(String name) {
                        if (name.equals("value")) {
                            return new AnnotationVisitor(api, super.visitArray(name)) {
                                @Override
                                public void visit(String name, Object value) {
                                    if (name == null && value instanceof Type) {
                                        types.add((Type) value);
                                    }
                                }
                            };
                        }
                        return null;
                    }
                });
                return types.isEmpty() ? null : types;
            }
        }
        return Collections.emptySet();
    }
}
