select avg(sum) filter (where r = 8) as sum_value,
       case
          when (nullif(max(sum),0) / nullif(min(sum), 0)) > 0
          then log10(nullif(max(sum), 0) / nullif(min(sum),0))
          else log10((nullif(max(sum), 0) - nullif(min(sum),0)) / least(abs(nullif(min(sum),0)), abs(nullif(max(sum),0))))
       end as sum_quality,
    avg(min) filter (where r = 8) as min_value,
       case
          when (nullif(max(min),0) / nullif(min(min), 0)) > 0
          then log10(nullif(max(min), 0) / nullif(min(min),0))
          else log10((nullif(max(min), 0) - nullif(min(min),0)) / least(abs(nullif(min(min),0)), abs(nullif(max(min),0))))
       end as min_quality,
    avg(max) filter (where r = 8) as max_value,
       case
          when (nullif(max(max),0) / nullif(min(max), 0)) > 0
          then log10(nullif(max(max), 0) / nullif(min(max),0))
          else log10((nullif(max(max), 0) - nullif(min(max),0)) / least(abs(nullif(min(max),0)), abs(nullif(max(max),0))))
       end as max_quality,
    avg(mean) filter (where r = 8) as mean_value,
       case
          when (nullif(max(mean),0) / nullif(min(mean), 0)) > 0
          then log10(nullif(max(mean), 0) / nullif(min(mean),0))
          else log10((nullif(max(mean), 0) - nullif(min(mean),0)) / least(abs(nullif(min(mean),0)), abs(nullif(max(mean),0))))
       end as mean_quality,
    avg(stddev) filter (where r = 8) as stddev_value,
       case
          when (nullif(max(stddev),0) / nullif(min(stddev), 0)) > 0
          then log10(nullif(max(stddev), 0) / nullif(min(stddev),0))
          else log10((nullif(max(stddev), 0) - nullif(min(stddev),0)) / least(abs(nullif(min(stddev),0)), abs(nullif(max(stddev),0))))
       end as stddev_quality,
    avg(median) filter (where r = 8) as median_value,
       case
          when (nullif(max(median),0) / nullif(min(median), 0)) > 0
          then log10(nullif(max(median), 0) / nullif(min(median),0))
          else log10((nullif(max(median), 0) - nullif(min(median),0)) / least(abs(nullif(min(median),0)), abs(nullif(max(median),0))))
       end as median_quality
from ( select r, sum(m), min(m), max(m), avg(m) as mean, stddev(m),
       percentile_cont(0.5) within group (order by m) as median
    from (select (%s / nullif(%s,0)) as m, resolution as r from stat_area) z
    group by r
    order by r ) z