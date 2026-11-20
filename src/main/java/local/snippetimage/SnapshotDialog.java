package local.snippetimage;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

final class SnapshotDialog extends DialogWrapper {
    private final List<SnapshotRenderer.Span> spans;
    private final Font font;
    private final Color background;
    private final Color foreground;
    private final int firstLine;
    private final int tabSize;
    private final JCheckBox numbers = new JCheckBox("Line numbers", true);
    private final JCheckBox transparent = new JCheckBox("Transparent padding");
    private final JComboBox<String> scale = new JComboBox<>(new String[]{"1x", "2x"});
    private final JLabel preview = new JLabel();
    private final JLabel status = new JLabel();
    private BufferedImage image;
    private final Action copy = new AbstractAction("Copy Image") {
        @Override public void actionPerformed(ActionEvent event) { copyImage(); }
    };

    SnapshotDialog(Project project, List<SnapshotRenderer.Span> spans, Font font, Color background,
                   Color foreground, int firstLine, int tabSize) {
        super(project, false);
        this.spans = List.copyOf(spans);
        this.font = font;
        this.background = background;
        this.foreground = foreground;
        this.firstLine = firstLine;
        this.tabSize = tabSize;
        setTitle("Snippet Image");
        setOKButtonText("Save PNG…");
        setCancelButtonText("Close");
        init();
        refresh();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEADING));
        options.add(numbers);
        options.add(transparent);
        JLabel scaleLabel = new JLabel("Scale:");
        scaleLabel.setLabelFor(scale);
        options.add(scaleLabel);
        options.add(scale);
        numbers.addActionListener(event -> refresh());
        transparent.addActionListener(event -> refresh());
        scale.addActionListener(event -> refresh());
        preview.setHorizontalAlignment(SwingConstants.CENTER);
        preview.getAccessibleContext().setAccessibleName("Code image preview");
        var scroll = new JBScrollPane(preview);
        scroll.setPreferredSize(new Dimension(760, 440));
        panel.add(options, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(status, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{copy, getOKAction(), getCancelAction()};
    }

    private void refresh() {
        image = null;
        preview.setIcon(null);
        try {
            image = SnapshotRenderer.render(spans, font, background, foreground, firstLine, tabSize,
                    numbers.isSelected(), transparent.isSelected(), scale.getSelectedIndex() + 1);
            double ratio = Math.min(1, Math.min(720.0 / image.getWidth(), 400.0 / image.getHeight()));
            Image thumbnail = ratio == 1 ? image : image.getScaledInstance(
                    Math.max(1, (int) (image.getWidth() * ratio)), Math.max(1, (int) (image.getHeight() * ratio)), Image.SCALE_SMOOTH);
            preview.setIcon(new ImageIcon(thumbnail));
            status.setText(image.getWidth() + " × " + image.getHeight() + " px — export uses full resolution");
        } catch (IllegalArgumentException exception) {
            status.setText(exception.getMessage());
        }
        setOKActionEnabled(image != null);
        copy.setEnabled(image != null);
    }

    @Override
    protected void doOKAction() {
        if (image == null) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Snippet Image");
        chooser.setFileFilter(new FileNameExtensionFilter("PNG image", "png"));
        chooser.setSelectedFile(new java.io.File("snippet-image.png"));
        if (chooser.showSaveDialog(getContentPanel()) != JFileChooser.APPROVE_OPTION) return;
        Path path = chooser.getSelectedFile().toPath();
        if (!path.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".png")) {
            path = path.resolveSibling(path.getFileName() + ".png");
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", bytes)) throw new IOException("PNG encoder is unavailable.");
            // Never overwrite a user's existing image, including files created after the chooser opens.
            Files.write(path, bytes.toByteArray(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            status.setText("Saved: " + path.toAbsolutePath());
        } catch (FileAlreadyExistsException exception) {
            Messages.showInfoMessage(getContentPanel(), "This file already exists. Choose a different name.", "Snippet Image");
        } catch (IOException | SecurityException exception) {
            Messages.showErrorDialog(getContentPanel(), "Could not save PNG: " + exception.getMessage(), "Snippet Image");
        }
    }

    private void copyImage() {
        if (image == null) return;
        BufferedImage snapshot = image;
        try {
            CopyPasteManager.getInstance().setContents(new Transferable() {
                @Override public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
                @Override public boolean isDataFlavorSupported(DataFlavor flavor) { return DataFlavor.imageFlavor.equals(flavor); }
                @Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
                    return snapshot;
                }
            });
            status.setText("Image copied to clipboard");
        } catch (IllegalStateException | java.awt.HeadlessException | SecurityException exception) {
            Messages.showErrorDialog(getContentPanel(), "Could not copy image: " + exception.getMessage(), "Snippet Image");
        }
    }

    @Override
    protected void dispose() {
        preview.setIcon(null);
        image = null;
        super.dispose();
    }
}
