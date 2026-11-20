package local.snippetimage;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.Messages;
import java.util.ArrayList;

public final class SnapshotAction extends DumbAwareAction {
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        event.getPresentation().setEnabled(editor instanceof EditorEx && editor.getSelectionModel().hasSelection());
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (!(editor instanceof EditorEx source) || !source.getSelectionModel().hasSelection()) return;
        var selection = source.getSelectionModel();
        if (selection.getBlockSelectionStarts().length > 1 || source.getCaretModel().getCaretCount() > 1) {
            Messages.showInfoMessage(event.getProject(), "Select one continuous range of code.", "Snippet Image");
            return;
        }
        int start = selection.getSelectionStart();
        int end = selection.getSelectionEnd();
        CharSequence document = source.getDocument().getImmutableCharSequence();
        if (end > start && document.charAt(end - 1) == '\n') end--;
        if (end <= start || end - start > 20_000 || source.getDocument().getLineNumber(end)
                - source.getDocument().getLineNumber(start) >= 300) {
            Messages.showInfoMessage(event.getProject(), "Select 1–300 lines, up to 20,000 characters.", "Snippet Image");
            return;
        }
        var spans = new ArrayList<SnapshotRenderer.Span>();
        HighlighterIterator iterator = source.getHighlighter().createIterator(start);
        int offset = start;
        while (offset < end) {
            if (iterator.atEnd()) {
                spans.add(new SnapshotRenderer.Span(document.subSequence(offset, end).toString(), null, 0));
                break;
            }
            int tokenEnd = Math.min(end, iterator.getEnd());
            if (tokenEnd <= offset) {
                iterator.advance();
                continue;
            }
            TextAttributes attributes = iterator.getTextAttributes();
            spans.add(new SnapshotRenderer.Span(document.subSequence(offset, tokenEnd).toString(),
                    attributes == null ? null : attributes.getForegroundColor(), attributes == null ? 0 : attributes.getFontType()));
            offset = tokenEnd;
            iterator.advance();
        }
        var scheme = source.getColorsScheme();
        var font = scheme.getFont(EditorFontType.PLAIN).deriveFont((float) scheme.getEditorFontSize());
        new SnapshotDialog(event.getProject(), spans, font, scheme.getDefaultBackground(), scheme.getDefaultForeground(),
                source.getDocument().getLineNumber(start) + 1, source.getSettings().getTabSize(event.getProject())).show();
    }
}
