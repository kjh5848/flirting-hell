create table app_users (
  id text primary key,
  firebase_uid text not null,
  status text not null,
  onboarding_completed boolean not null default false,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  last_seen_at timestamptz,
  deleted_at timestamptz
);

create unique index uq_app_users_firebase_uid on app_users (firebase_uid);
create index idx_app_users_status_created on app_users (status, created_at);

create table linked_auth_providers (
  id text primary key,
  user_id text not null references app_users (id),
  provider text not null,
  provider_user_id text not null,
  created_at timestamptz not null
);

create unique index uq_auth_provider_user on linked_auth_providers (provider, provider_user_id);
create unique index uq_auth_user_provider on linked_auth_providers (user_id, provider);

create table user_profiles (
  user_id text primary key references app_users (id),
  nickname text,
  speech_style text,
  dating_style text,
  guidance_level text not null,
  preferred_partner_style text,
  avoid_advice text,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create table usage_days (
  id text primary key,
  user_id text not null references app_users (id),
  usage_date date not null,
  free_analysis_used_count integer not null default 0,
  reward_ad_used_count integer not null default 0,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create unique index uq_usage_days_user_date on usage_days (user_id, usage_date);
