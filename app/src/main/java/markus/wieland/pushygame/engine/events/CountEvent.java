package markus.wieland.pushygame.engine.events;

import markus.wieland.pushygame.engine.entity.movable.Count;

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
            CountEventThread thread = new CountEventThread();
            thread.start();
            return;
        }
        setCurrentCount(currentCount + 1);
    }

    private class CountEventThread extends Thread {

        @Override
        public void run() {
            try {
                sleep(500);
                for (Count countOfList : game.getEntityManager().getOfType(Count.class)) {
                    countOfList.setUncovered(false);
                    game.getActivity().runOnUiThread(() -> game.getEntityManager().invalidate(countOfList));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

    }

}

