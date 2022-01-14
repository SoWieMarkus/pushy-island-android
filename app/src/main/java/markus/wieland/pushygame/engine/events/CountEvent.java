package markus.wieland.pushygame.engine.events;

import android.app.Activity;
import android.content.Context;

import markus.wieland.pushygame.engine.entity.movable.Count;

import static java.lang.Thread.sleep;

public class CountEvent extends Event {

    private static int currentCount = 1;

    public static void setCurrentCount(int currentCount) {
        CountEvent.currentCount = currentCount;
    }

    private final int count;

    public CountEvent(int count) {
        super();
        this.count = count;
    }

    @Override
    public void execute() {
        if (count != currentCount) {
            setCurrentCount(1);
            for (Count countOfList : game.getEntityManager().getOfType(Count.class)) {
                Thread thread = new Thread(() -> {
                    try {
                        sleep(500);
                        countOfList.setUncovered(false);
                        Context context = game.getEntityManager().getView(countOfList.getCoordinate()).getContext();
                        ((Activity) context).runOnUiThread(() -> game.getEntityManager().invalidate(countOfList));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
            return;
        }
        setCurrentCount(currentCount + 1);
    }
}
