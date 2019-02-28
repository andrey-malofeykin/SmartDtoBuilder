package com.github.oldtown.phpstorm.smartdtobuilder.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CreateDtoBuilderDialog extends DialogWrapper {

    public CreateDtoBuilderDialog(@Nullable Project project) {
        super(project);
        setTitle("Create DTO");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return null;
    }
}
