package io.kodokojo.model;

public class UpdateData<T> {

    private final T oldData;

    private final T newData;

    public UpdateData(T oldData, T newData) {
        this.oldData = oldData;
        this.newData = newData;
    }

    public T getOldData() {
        return oldData;
    }

    public T getNewData() {
        return newData;
    }

    @Override
    public String toString() {
        return "UpdateData{" +
                "oldData=" + oldData +
                ", newData=" + newData +
                '}';
    }
}
