package xin.tianhui.fun.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.EditorModificationUtilEx;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CamelCaseAction extends TextComponentEditorAction {

    private static final String UNDERSCORES_SYMBOL = "_";

    private static final String SPACE_SYMBOL = " ";

    private static final String CONNECTION_SYMBOL = "-";

    protected CamelCaseAction() {
        super(new EditorActionHandler() {
            @Override
            protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
                String text = editor.getSelectionModel().getSelectedText();
                String appendText = "";
                String newText = text;
                if (StringUtils.isNotBlank(text)) {
                    Pattern p = Pattern.compile("^\\W+");
                    Matcher m = p.matcher(text);
                    if (m.find()) {
                        appendText = m.group(0);
                    }
                    //remove all special chars
                    text = text.replaceAll("^\\W+", "");
                    boolean upperCase = StringUtils.equals(text, text.toUpperCase());
                    boolean lowerCase = StringUtils.equals(text, text.toLowerCase());
                    boolean underscores = StringUtils.contains(text, UNDERSCORES_SYMBOL);
                    boolean connection = StringUtils.contains(text, CONNECTION_SYMBOL);
                    boolean space = StringUtils.contains(text, SPACE_SYMBOL);
                    if (lowerCase && underscores) {  // snake_case
                        newText = text.replace(UNDERSCORES_SYMBOL, " ");
                    } else if (lowerCase && space) { // space case
                        newText = WordUtils.capitalize(text);
                    } else if (Character.isUpperCase(text.charAt(0)) && Character.isLowerCase(text.charAt(1)) && space) {
                        newText = text.toLowerCase().replace(' ', '-');
                    } else if (lowerCase && connection) { // kebab-case
                        newText = text.replace(CONNECTION_SYMBOL, UNDERSCORES_SYMBOL).toUpperCase();
                    } else if ((upperCase && underscores) || (lowerCase && !underscores && !space) || (upperCase && !space)) {
                        newText = toCamelCase(text.toLowerCase());
                    } else if (!upperCase && text.substring(0, 1).equals(text.substring(0, 1).toUpperCase()) && !underscores) {
                        newText = text.substring(0, 1).toLowerCase() + text.substring(1);
                    } else {
                        newText = toSnakeCase(text);
                    }
                }
                if (StringUtils.isNotBlank(newText)) {
                    String replacement = appendText + newText;
                    WriteAction.run(() -> {
                        int start = editor.getSelectionModel().getSelectionStart();
                        EditorModificationUtilEx.insertStringAtCaret(editor, replacement);
                        editor.getSelectionModel().setSelection(start, start + replacement.length());
                    });
                }
            }
        });
    }

    private static String toSnakeCase(String in) {
        in = in.replaceAll(" +", "");
        StringBuilder result = new StringBuilder("" + Character.toLowerCase(in.charAt(0)));
        for (int i = 1; i < in.length(); i++) {
            char c = in.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append(UNDERSCORES_SYMBOL).append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private static String toCamelCase(String in) {
        StringBuilder camelCased = new StringBuilder();
        String[] tokens = in.split(UNDERSCORES_SYMBOL);
        for (String token : tokens) {
            if (token.length() >= 1) {
                camelCased.append(token.substring(0, 1).toUpperCase()).append(token.substring(1));
            } else {
                camelCased.append(UNDERSCORES_SYMBOL);
            }
        }
        return camelCased.toString();
    }

}
