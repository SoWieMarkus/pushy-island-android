package markus.wieland.pushygame.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.LayoutRes;

import markus.wieland.pushygame.R;

public class Dialog {

    private final AlertDialog.Builder builder;

    private String message;

    private String okMessage;
    private String declineMessage;

    private DialogInteractionListener dialogInteractionListenerOk;
    private DialogInteractionListener dialogInteractionListenerDecline;

    @LayoutRes
    private int layout;

    private final Context context;

    public Dialog(Context context) {
        this.builder = new AlertDialog.Builder(context);
        okMessage = context.getString(R.string.dialog_yes);
        this.context = context;
        declineMessage = context.getString(R.string.dialog_no);
        this.layout = -1;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public Dialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public Dialog setOkMessage(String okMessage) {
        this.okMessage = okMessage;
        return this;
    }

    public Dialog setDeclineMessage(String declineMessage) {
        this.declineMessage = declineMessage;
        return this;
    }

    public Dialog setOkEvent(DialogInteractionListener dialogInteractionListenerOk) {
        this.dialogInteractionListenerOk = dialogInteractionListenerOk;
        return this;
    }

    public Dialog setDeclineEvent(DialogInteractionListener dialogInteractionListenerDecline) {
        this.dialogInteractionListenerDecline = dialogInteractionListenerDecline;
        return this;
    }

    public AlertDialog getDialog() {
        if (message != null) {
            builder.setMessage(message);
        }

        if (layout != -1) {
            View viewInflated = LayoutInflater.from(context).inflate(layout, null);
            initializeView(viewInflated);
            builder.setView(viewInflated);
        }


        builder.setPositiveButton(
                okMessage, (dialog, id) -> {
                    if (dialogInteractionListenerOk != null) {
                        dialogInteractionListenerOk.onClick(this);
                    }
                    dialog.cancel();
                });
        builder.setNegativeButton(declineMessage,
                (dialog, id) -> {
                    if (dialogInteractionListenerDecline != null) {
                        dialogInteractionListenerDecline.onClick(this);
                    }
                    dialog.cancel();
                });
        return builder.create();
    }

    protected void initializeView(View view) {
        // will be used from child classes
    }

}
