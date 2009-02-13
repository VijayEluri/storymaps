package storymaps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import storymaps.ui.Fonts;

/**
 *
 * @author seanh
 */
public class FunctionEditor {

    private Function function;
    private JPanel panel = new JPanel();
    private JPanel subpanel = new JPanel();
    private JLabel title;
    private JLabel icon;
    private JTextArea desc;
    private JTextArea editor;
            
    public FunctionEditor(Function function) {
        this(function,"");
    }
    
    public FunctionEditor(Function function, String text) {
        this.function = function;
        FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER);
        panel.setLayout(flowLayout);
        //panel.setBackground(Color.WHITE);
        BorderLayout borderLayout = new BorderLayout();
        subpanel.setLayout(borderLayout);
        subpanel.setBackground(Color.WHITE);
        
        title = new JLabel(function.getFriendlyName());
        title.setFont(Fonts.LARGE);
        subpanel.add(title,BorderLayout.NORTH);        
        ImageIcon imageIcon = new ImageIcon(function.getImage(), "Illustation for function.");
        icon = new JLabel(imageIcon);
        subpanel.add(icon,BorderLayout.CENTER);
        desc = new JTextArea(6,20);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setText(function.getFriendlyDescription());
        desc.setFont(Fonts.NORMAL);
        subpanel.add(desc,BorderLayout.SOUTH);
        
        panel.add(subpanel);        
        editor = new JTextArea(6,45);
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        editor.setText(text);
        editor.setFont(Fonts.LARGE);
        JScrollPane scrollPane = new JScrollPane(editor);
        panel.add(scrollPane);
    }
        
    public JComponent getComponent() {
        return panel;
    }
    
    public Function getFunction() {
        return function;
    }
    
    public String getText() {
        return editor.getText();
    }
    
    public void focus() {
        editor.requestFocusInWindow();
    }

    /**
     * Return true if obj is equivalent to this function editor, false
     * otherwise.
     */
    public boolean compare(Object obj) {
        if (!(obj instanceof FunctionEditor)) {
            return false;
        } else {
            FunctionEditor f = (FunctionEditor) obj;
            if (!(f.getFunction().compare(getFunction()))) { return false; }
            if (!(f.getText().equals(getText()))) { return false; }
            return true;
        }        
    }    

    public static class Memento {
        public Object function_memento;
        public String text;
        public Memento(Object function_memento, String text) {
            this.function_memento = function_memento;
            this.text = text;
        }
        @Override
        public String toString() {
            String string = "<div class='FunctionEditor'>\n";
            string += this.function_memento.toString();
            string += "<div class='user_text'>" + this.text + "</div><!--user_text-->\n";
            string += "</div><!--FunctionEditor-->\n";
            return string;
        }
    }    
    
    public Object saveToMemento() {
        return new Memento(this.function.saveToMemento(),
                this.editor.getText());
    }
    
    /** 
     * Return a new FunctionEditor constructed from a memento object.
     */
    public static FunctionEditor newFromMemento(Object o) {
        if (!(o instanceof Memento)) {
            throw new IllegalArgumentException("Argument not instanceof Memento.");
        }
        else {
            Memento m = (Memento) o;
            Function f = Function.newFromMemento(m.function_memento);
            return new FunctionEditor(f,m.text);
        }
    }        
}