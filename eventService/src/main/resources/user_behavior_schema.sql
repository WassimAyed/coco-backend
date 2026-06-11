CREATE TABLE IF NOT EXISTS user_behavior (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  event_id BIGINT NOT NULL,
  category_id BIGINT,
  action_type ENUM('VIEW','PARTICIPATE','BOOKMARK') NOT NULL,
  last_lat DOUBLE,
  last_lng DOUBLE,
  action_date DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE INDEX idx_user_behavior_user_id ON user_behavior(user_id);
CREATE INDEX idx_user_behavior_event_id ON user_behavior(event_id);
CREATE INDEX idx_user_behavior_action_type ON user_behavior(action_type);
CREATE INDEX idx_user_behavior_user_action ON user_behavior(user_id, action_type);
