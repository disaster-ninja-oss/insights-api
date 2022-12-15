INSERT INTO %s
(param_id, param_label, copyrights, direction, is_base, param_uuid, owner, state, is_public,
 allowed_users, date, description, coverage, update_frequency, application, unit_id, last_updated)
VALUES (:id, :label, :copyrights::json, :direction::json, :isBase, gen_random_uuid(), :owner, 'NEW', :isPublic,
        :allowedUsers::json, now(), :description, :coverage, :updateFrequency, :application,
        :unitId, :lastUpdated) RETURNING param_uuid