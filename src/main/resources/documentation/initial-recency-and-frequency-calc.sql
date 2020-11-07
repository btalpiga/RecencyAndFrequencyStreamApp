begin;

drop table if exists recency_and_frequency_start;

UPDATE config_parameters set value = date_trunc('milliseconds', now()-'2 year'::interval)::text where key = 'RECENCY_AND_FREQUENCY_DECREMENT_FROM';

create table recency_and_frequency_start as
select ca.system_id, ca.consumer_id, max(external_system_date) as recency, count(1) as frequency
from consumer_actions ca join affinity.action_scores acts on ca.action_id = acts.action_id
where external_system_date >= now()-'2 year'::interval and external_system_date < now() and
    ((ca.id<=:lastRmcActionId and ca.system_id = 1) or (ca.id<=:lastRrpActionId and ca.system_id = 2) )
group by ca.system_id, ca.consumer_id;


alter table recency_and_frequency_start add column id serial;

commit;