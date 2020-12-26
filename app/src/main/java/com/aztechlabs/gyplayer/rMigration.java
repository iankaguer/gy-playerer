package com.aztechlabs.gyplayer;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class rMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();


        if (oldVersion == 1) {
            schema.get("Person")
                    .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
                    .addField("name", String.class)
                    .addField("uri", String.class)
                    .addField("artist", String.class)
                    .addField("isfavorite", boolean.class);
            oldVersion++;
        }
    }
}