INSERT INTO %s (param_id, label, copyrights, direction, is_base, internal_id,
                external_id, owner, state, is_public, allowed_users, date, description,
                coverage, update_frequency, application, unit_id, last_updated)
VALUES (?, ?, ?::json, ?::json, ?, gen_random_uuid(), ?::uuid, ?, 'NEW', ?, ?::json, now(), ?, ?, ?, ?::json, ?, ?::timestamptz)
RETURNING internal_id::text;