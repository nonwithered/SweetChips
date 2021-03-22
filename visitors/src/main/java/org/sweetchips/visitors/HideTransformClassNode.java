package org.sweetchips.visitors;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.sweetchips.annotations.Hide;

import java.util.Iterator;
import java.util.List;

public class HideTransformClassNode extends ClassNode {

    public HideTransformClassNode(int api) {
        super(api);
    }

    @Override
    public void accept(ClassVisitor cv) {
        acceptClass();
        acceptField();
        acceptMethod();
        super.accept(cv);
    }

    private void acceptClass() {
        if (invisibleAnnotations == null) {
            return;
        }
        boolean hide = false;
        @SuppressWarnings("unchecked")
        Iterator<AnnotationNode> itr = invisibleAnnotations.iterator();
        while ((itr.hasNext())) {
            AnnotationNode an = itr.next();
            if (an.desc.equals("L" + Hide .class.getName().replace(".", "/") + ";")) {
                itr.remove();
                hide = true;
                break;
            }
        }
        if (hide) {
            access |= Opcodes.ACC_SYNTHETIC;
        }
    }

    private void acceptField() {
        @SuppressWarnings("unchecked")
        List<FieldNode> fields = this.fields;
        fields.forEach(it -> {
            if (it.invisibleAnnotations == null) {
                return;
            }
            boolean hide = false;
            @SuppressWarnings("unchecked")
            Iterator<AnnotationNode> itr = it.invisibleAnnotations.iterator();
            while (itr.hasNext()) {
                AnnotationNode an = itr.next();
                if (an.desc.equals("L" + Hide .class.getName().replace(".", "/") + ";")) {
                    itr.remove();
                    hide = true;
                    break;
                }
            }
            if (hide) {
                it.access |= Opcodes.ACC_SYNTHETIC;
            }
        });
    }

    private void acceptMethod() {
        @SuppressWarnings("unchecked")
        List<MethodNode> methods = this.methods;
        methods.forEach(it -> {
            if (it.invisibleAnnotations == null) {
                return;
            }
            boolean hide = false;
            @SuppressWarnings("unchecked")
            Iterator<AnnotationNode> itr = it.invisibleAnnotations.iterator();
            while (itr.hasNext()) {
                AnnotationNode an = itr.next();
                if (an.desc.equals("L" + Hide.class.getName().replace(".", "/") + ";")) {
                    itr.remove();
                    hide = true;
                    break;
                }
            }
            if (hide) {
                it.access |= Opcodes.ACC_SYNTHETIC;
            }
        });
    }
}
