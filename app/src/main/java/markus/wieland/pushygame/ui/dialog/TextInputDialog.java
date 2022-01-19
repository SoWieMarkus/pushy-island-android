package markus.wieland.pushygame.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import markus.wieland.pushygame.R;

public class TextInputDialog extends Dialog {

    private EditText editText;
    private String text;

    public TextInputDialog(Context context, String text) {
        super(context);
        setLayout(R.layout.dialog_text_input);
        setText(text);
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    @Override
    protected void initializeView(View view) {
        super.initializeView(view);
        editText = view.findViewById(R.id.dialog_input_text_text);
        editText.setText(text);
        editText.setSelection(text.length());
    }

    public String getText() {
        return editText.getText().toString().trim();
    }
}


