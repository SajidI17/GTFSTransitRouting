CREATE TABLE routes
(
  route_id          text PRIMARY KEY,
  agency_id         integer NULL,
  route_short_name  text NOT NULL,
  route_long_name   text NULL,
  route_desc        text NULL,
  route_type        integer NULL,
  route_url         text NULL,
  route_color       text NULL,
  route_text_color  text NULL,
  route_sort_order	integer NULL,
  continuous_pickup integer NULL,
  continuous_drop_off	integer NULL,
  network_id			integer NULL
);

CREATE TABLE stop_times
(
  trip_id           text NOT NULL,
  arrival_time      interval NOT NULL,
  departure_time    interval NOT NULL,
  stop_id           text NOT NULL,
  stop_sequence     integer NOT NULL,
  stop_headsign     text NULL,
  pickup_type       integer NULL,
  drop_off_type     integer NULL,
  shape_dist_traveled double precision NULL,
  timepoint				integer NULL
);

CREATE TABLE stops
(
  stop_id           text PRIMARY KEY,
  stop_code         text NULL,
  stop_name         text NOT NULL,
  tts_stop_name		text NOT NULL,
  stop_desc         text NULL,
  stop_lat          double precision NOT NULL,
  stop_lon          double precision NOT NULL,
  zone_id           text NULL,
  stop_url          text NULL,
  location_type     text NULL,
  parent_station    text NULL,
  stop_timezone		text NULL,
  wheelchair_boarding	text NULL,
  level_id				text NULL,
  platform_code			text NULL
);

CREATE TABLE trips
(
  route_id          text NOT NULL,
  service_id        text NOT NULL,
  trip_id           text NOT NULL PRIMARY KEY,
  trip_headsign     text NULL,
  trip_short_name	text NULL,
  direction_id      boolean NOT NULL,
  block_id          text NULL,
  shape_id          text NULL,
  wheelchair_accessible text NULL,
  bikes_allowed			text NULL
);

CREATE TABLE calendar
(
  service_id text PRIMARY KEY,
  monday boolean NOT NULL,
  tuesday boolean NOT NULL,
  wednesday boolean NOT NULL,
  thursday boolean NOT NULL,
  friday boolean NOT NULL,
  saturday boolean NOT NULL,
  sunday boolean NOT NULL,
  start_date numeric(8) NOT NULL,
  end_date numeric(8) NOT NULL
);

