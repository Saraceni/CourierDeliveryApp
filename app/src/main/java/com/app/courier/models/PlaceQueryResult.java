package com.app.courier.models;

import org.json.JSONObject;

/**
 * Created by rafaelgontijo on 24/10/17.
 */

public class PlaceQueryResult {

    private String description;
    private String id;

    public PlaceQueryResult(JSONObject data) {
        try {
            this.description = data.getString("description");
            this.id = data.getString("id");
        } catch (Exception exc){

        }

    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        return this.id;
    }
}
