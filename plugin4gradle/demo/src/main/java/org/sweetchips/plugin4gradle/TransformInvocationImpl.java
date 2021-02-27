package org.sweetchips.plugin4gradle;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.SecondaryInput;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

final class TransformInvocationImpl implements TransformInvocation {

    private final Collection<TransformInput> mInputs;

    TransformInvocationImpl(Path path, Path next, Collection<Path> dirs) {
        mInputs = dirs.stream()
                .map(it -> new TransformInputImpl(path.resolve(it), next.resolve(it)))
                .collect(Collectors.toList());
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public Collection<TransformInput> getInputs() {
        return mInputs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<TransformInput> getReferencedInputs() {
        return (Set<TransformInput>) Collections.EMPTY_SET;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<SecondaryInput> getSecondaryInputs() {
        return (Set<SecondaryInput>) Collections.EMPTY_SET;
    }

    @Override
    public TransformOutputProvider getOutputProvider() {
        return null;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }
}
