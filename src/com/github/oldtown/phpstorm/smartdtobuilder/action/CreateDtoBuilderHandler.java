package com.github.oldtown.phpstorm.smartdtobuilder.action;

import com.github.oldtown.phpstorm.smartdtobuilder.intentions.generators.DtoBuilderGenerator;
import com.github.oldtown.phpstorm.smartdtobuilder.ui.CreateDtoBuilderDialog;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.actions.PhpNamedElementNode;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.TreeMap;

class CreateDtoBuilderHandler implements LanguageCodeInsightActionHandler {
    private static final Logger LOG = Logger.getInstance("#com.github.oldtown.phpstorm.smartdtobuilder.action.CreateDtoBuilderHandler");

    @Override
    public boolean isValidFor(Editor editor, PsiFile file) {
        if (!(file instanceof PhpFile)) {
            return false;
        } else {
            PhpClass phpClass = PhpCodeEditUtil.findClassAtCaret(editor, file);
            return phpClass != null && !phpClass.isInterface();
        }
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PhpFile phpFile = (PhpFile)file;
        PhpClass targetClass = PhpCodeEditUtil.findClassAtCaret(editor, phpFile);

        if (targetClass != null) {
            PhpNamedElementNode[] fieldsToShow = this.collectFields(targetClass);
        }

        createDialog(project).show();
    }

    @NotNull
    private PhpNamedElementNode[] collectFields(@NotNull PhpClass phpClass) {

        TreeMap<String, PhpNamedElementNode> nodes = new TreeMap<>();
        Collection<Field> fields = phpClass.getFields();

        for (Field field : fields) {
            if (!field.isConstant() && this.isSelectable(phpClass, field)) {
                nodes.put(field.getName(), new PhpNamedElementNode(field));
            }
        }

        return nodes.values().toArray(new PhpNamedElementNode[0]);
    }

    private boolean isSelectable(PhpClass phpClass, Field field) {
        DtoBuilderGenerator generator = new DtoBuilderGenerator(phpClass, field);
        return generator.findGetters().length == 0 && generator.findSetters().length == 0;
    }

    private CreateDtoBuilderDialog createDialog(Project project) {
        return new CreateDtoBuilderDialog(project);
    }
}
