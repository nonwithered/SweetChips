package org.sweetchips.plugin4gradle;

import org.sweetchips.plugin4gradle.hook.TransformInput;
import org.sweetchips.plugin4gradle.hook.TransformInvocation;
import org.sweetchips.plugin4gradle.hook.TransformOutputProvider;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

final class TransformInvocationImpl implements TransformInvocation {

    private final Collection<TransformInput> mInputs;

    private final TransformOutputProvider mOutputProvider;

    TransformInvocationImpl(Path path, Path next, Collection<Path> dirs) {
        mInputs = dirs.stream()
                .map(it -> new TransformInputImpl(path.resolve(it), next.resolve(it)))
                .collect(Collectors.toList());
        mOutputProvider = new TransformOutputProviderImpl(path, next);
    }

    @Override
    public Collection<TransformInput> getInputs() {
        return mInputs;
    }

    @Override
    public TransformOutputProvider getOutputProvider() {
        return mOutputProvider;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }
}
