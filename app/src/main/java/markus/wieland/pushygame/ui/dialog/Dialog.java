package markus.wieland.pushygame.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;

import markus.wieland.pushygame.R;

public class Dialog {

    private final AlertDialog.Builder builder;

    private String message;

    private String okMessage;
    private String declineMessage;

    private DialogInteractionListener dialogInteractionListenerOk;
    private DialogInteractionListener dialogInteractionListenerDecline;


    public Dialog(Context context) {
        this.builder = new AlertDialog.Builder(context);
        okMessage = context.getString(R.string.dialog_yes);
        declineMessage = context.getString(R.string.dialog_no);
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
        builder.setPositiveButton(
                okMessage, (dialog, id) -> {
                    if (dialogInteractionListenerOk != null) {
                        dialogInteractionListenerOk.onClick(null);
                    }
                    dialog.cancel();
                });
        builder.setNegativeButton(declineMessage,
                (dialog, id) -> {
                    if (dialogInteractionListenerDecline != null) {
                        dialogInteractionListenerDecline.onClick(null);
                    }
                    dialog.cancel();
                });
        return builder.create();
    }

}
