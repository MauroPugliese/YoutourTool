package it.gangoffive.eculture.model;

import java.util.List;
import java.util.Objects;

public class GraphModel {

    private final String mType;
    private final String mId;
    private final String mMessage;
    private final List<String> mPlace;
    private final int mStatus;

    public GraphModel(String mType, String mId, String mMessage, List<String> mPlace, int mStatus) {
        this.mType = mType;
        this.mId = mId;
        this.mMessage = mMessage;
        this.mPlace = mPlace;
        this.mStatus = mStatus;
    }

    public String getmId() {
        return mId;
    }

    public String getmType() {
        return mType;
    }

    public String getMessage() {
        return mMessage;
    }

    public List<String> getmPlace() {
        return mPlace;
    }

    public int getStatus() {
        return mStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphModel that = (GraphModel) o;
        return mStatus == that.mStatus && Objects.equals(mType, that.mType) && Objects.equals(mId, that.mId) && Objects.equals(mMessage, that.mMessage) && Objects.equals(mPlace, that.mPlace);
    }

}