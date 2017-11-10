/*
 * Bao Lab 2016
 */

package application_src.application_model.resources;

import java.util.ListResourceBundle;

import application_src.application_model.logic.lineage.LineageData;

public class NucleiMgrAdapterResource extends ListResourceBundle {

    private final LineageData lineageData;

    public NucleiMgrAdapterResource(final LineageData lineageData) {
        this.lineageData = lineageData;
    }

    @Override
    protected Object[][] getContents() {
        return new Object[][]{{"lineageData", lineageData}};
    }
}