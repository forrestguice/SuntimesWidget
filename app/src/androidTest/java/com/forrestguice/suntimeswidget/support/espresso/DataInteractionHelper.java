package com.forrestguice.suntimeswidget.support.espresso;

import androidx.test.espresso.DataInteraction;

public class DataInteractionHelper
{
    public interface DataInteractionInterface {
        DataInteraction get();
    }

    public static DataInteractionInterface wrap(final DataInteraction dataInteraction) {
        return new DataInteractionInterface() {
            @Override
            public DataInteraction get() {
                return dataInteraction;
            }
        };
    }

}