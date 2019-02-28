package com.github.oldtown.phpstorm.smartdtobuilder.intentions.generators;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class DtoBuilderGenerator {
    @NotNull
    private final PhpClass myTargetClass;
    @NotNull
    private final Field myField;

    public DtoBuilderGenerator(@NotNull PhpClass myTargetClass, @NotNull Field myField) {
        this.myTargetClass = myTargetClass;
        this.myField = myField;
    }

    @NotNull
    public Method[] findGetters() {
        return this.findFieldAccessMethods("PHP Getter Method");
    }

    @NotNull
    public Method[] findSetters() {
        return this.findFieldAccessMethods("PHP Setter Method");
    }


    @NotNull
    private Method[] findFieldAccessMethods(String templateName) {
        List<Method> accessMethods = new ArrayList<>();
        String[] expectedNames = this.getPossibleAccessorMethodNames(templateName);

        return accessMethods.toArray(Method.EMPTY);
    }


    @NotNull
    private String[] getPossibleAccessorMethodNames(@NotNull String templateName) {
        Set<String> allNames = new HashSet<>();
        String prefix = "PHP Getter Method".equals(templateName) ? "get" : "set";
        allNames.add((prefix + this.myField.getName()).toUpperCase());
        FileTemplate currTemplate = FileTemplateManager.getInstance(this.myField.getProject()).getCodeTemplate(templateName);
        this.collectMethodNamesFromTemplate(currTemplate.getText(), 0, this.myField, allNames, templateName);

        return ArrayUtil.toStringArray(allNames);
    }


    private void collectMethodNamesFromTemplate(@NotNull String templateText, int startOffset, Field field, Set<String> result, String templateName) {
        int functionStart = templateText.indexOf("function", startOffset);
        if (functionStart >= 0) {
            functionStart += "function".length();
            int argListStart = templateText.indexOf(40, functionStart);
            if (argListStart >= 0) {
                String methodName = templateText.substring(functionStart, argListStart).trim();
                Properties attributes = this.getAccessMethodAttributes(PhpCodeInsightUtil.findScopeForUseOperator(field), templateName);

                String property;
                String value;
                for(Iterator name= attributes.stringPropertyNames().iterator(); name.hasNext(); methodName = methodName.replace("${" + property + "}", value)) {
                    property = (String)name.next();
                    value = attributes.getProperty(property, "");
                }

                methodName = methodName.replace("_", "").toUpperCase();
                result.add(methodName);
            }

            this.collectMethodNamesFromTemplate(templateText, functionStart, field, result, templateName);
        }
    }



    private Properties getAccessMethodAttributes(@Nullable PhpPsiElement scopeForUseOperator, String templateName) {
        Properties attributes = new Properties();
        String typeHint = this.fillAttributes(scopeForUseOperator, attributes, templateName);
        this.addTypeHintsAndReturnType(attributes, typeHint);
        return attributes;
    }

    @NotNull
    private String fillAttributes(@Nullable PhpPsiElement scopeForUseOperator, Properties attributes, String templateName) {
        String fieldName = this.myField.getName();
        String typeHint = PhpCodeUtil.getTypeHint(this.myField, scopeForUseOperator);
        String paramName = StringUtil.trimStart(fieldName, "_");
        attributes.setProperty("FIELD_NAME", fieldName);
        attributes.setProperty("PARAM_NAME", paramName);
        String name = PhpCodeUtil.camelCaps(fieldName);
        boolean isBooleanWithIsPrefix = false;
        if (templateName.equals("PHP Getter Method") && name.startsWith("Is") && name.length() > 2 && Character.isUpperCase(name.toCharArray()[2])) {
            name = "i" + name.substring(1);
            isBooleanWithIsPrefix = true;
        }

        attributes.setProperty("NAME", name);
        attributes.setProperty("TYPE_HINT", typeHint);
        attributes.setProperty("STATIC", this.myField.getModifier().isStatic() ? "static" : "");
        attributes.setProperty("CLASS_NAME", this.myTargetClass.getName());
        attributes.setProperty("GET_OR_IS", "bool".equalsIgnoreCase(typeHint) ? (isBooleanWithIsPrefix ? "" : "is") : "get");


        return typeHint;
    }

    private void addTypeHintsAndReturnType(Properties attributes, String typeHint) {
        Project project = this.myField.getProject();
        if (PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel().hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS) && isDocTypeConvertable(typeHint)) {
            attributes.setProperty("SCALAR_TYPE_HINT", convertDocTypeToHint(project, typeHint));
        }

        boolean hasFeatureVoid = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel().hasFeature(PhpLanguageFeature.RETURN_VOID);
        if (hasFeatureVoid) {
            attributes.setProperty("VOID_RETURN_TYPE", "void");
        }

        if (PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel().hasFeature(PhpLanguageFeature.RETURN_TYPES) && isDocTypeConvertable(typeHint)) {
            attributes.setProperty("RETURN_TYPE", convertDocTypeToHint(project, typeHint));
        }

    }

    @NotNull
    private static String convertDocTypeToHint(Project project, String typeHint) {
        String hint = typeHint.contains("[]") ? "array" : typeHint;
        hint = hint.contains("boolean") ? "bool" : hint;
        if (typeWithNull(typeHint)) {
            hint = convertNullableType(project, hint);
        }


        return hint;
    }

    private static boolean typeWithNull(String typeHint) {
        return typeHint.split(Pattern.quote("|")).length == 2 && typeHint.toUpperCase().contains("NULL");
    }

    private static String convertNullableType(Project project, String typeHint) {
        String[] split = typeHint.split(Pattern.quote("|"));
        boolean hasNullableTypeFeature = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel().hasFeature(PhpLanguageFeature.NULLABLES);
        return split[0].equalsIgnoreCase("null") ? (hasNullableTypeFeature ? "?" : "") + split[1] : (hasNullableTypeFeature ? "?" : "") + split[0];
    }

    private static boolean isDocTypeConvertable(String typeHint) {
        return !typeHint.equalsIgnoreCase("mixed") && !typeHint.equalsIgnoreCase("static") && !typeHint.equalsIgnoreCase("true") && !typeHint.equalsIgnoreCase("false") && !typeHint.equalsIgnoreCase("null") && (!typeHint.contains("|") || typeWithNull(typeHint));
    }
}
