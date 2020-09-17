package org.sweetchips.plugin4gradle;

import com.android.build.gradle.BaseExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class UnionPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        BaseExtension baseExtension = (BaseExtension) project.getProperties().get("android");
        project.getExtensions().create(Util.NAME, UnionExtension.class);
        if (UnionContext.EXT.isEnable()) {
            if (baseExtension != null) {
                baseExtension.registerTransform(new UnionTransform());
            }
        }
    }

}
