select avg(sum) filter (where r = 8) as sum_value, case
    --if value is null, no need to calculate quality
          when avg(sum) filter (where r = 8) is null then null
          when (nullif(max(sum),0) / nullif(min(sum), 0)) > 0
          then log10(nullif(max(sum), 0) / nullif(min(sum),0))
          else log10((nullif(max(sum), 0) - nullif(min(sum),0)) / least(abs(nullif(min(sum),0)), abs(nullif(max(sum),0))))
end
as sum_quality,
    avg(min) filter (where r = 8) as min_value,
       case
        --if value is null, no need to calculate quality
          when avg(min) filter (where r = 8) is null then null
          when (nullif(max(min),0) / nullif(min(min), 0)) > 0
          then log10(nullif(max(min), 0) / nullif(min(min),0))
          else log10((nullif(max(min), 0) - nullif(min(min),0)) / least(abs(nullif(min(min),0)), abs(nullif(max(min),0))))
end
as min_quality,
    avg(max) filter (where r = 8) as max_value,
       case
          --if value is null, no need to calculate quality
          when avg(max) filter (where r = 8) is null then null
          when (nullif(max(max),0) / nullif(min(max), 0)) > 0
          then log10(nullif(max(max), 0) / nullif(min(max),0))
          else log10((nullif(max(max), 0) - nullif(min(max),0)) / least(abs(nullif(min(max),0)), abs(nullif(max(max),0))))
end
as max_quality,
    avg(mean) filter (where r = 8) as mean_value,
       case
          --if value is null, no need to calculate quality
          when avg(mean) filter (where r = 8) is null then null
          when (nullif(max(mean),0) / nullif(min(mean), 0)) > 0
          then log10(nullif(max(mean), 0) / nullif(min(mean),0))
          else log10((nullif(max(mean), 0) - nullif(min(mean),0)) / least(abs(nullif(min(mean),0)), abs(nullif(max(mean),0))))
end
as mean_quality,
    avg(stddev) filter (where r = 8) as stddev_value,
       case
          --if value is null, no need to calculate quality
          when avg(stddev) filter (where r = 8) is null then null
          when (nullif(max(stddev),0) / nullif(min(stddev), 0)) > 0
          then log10(nullif(max(stddev), 0) / nullif(min(stddev),0))
          else log10((nullif(max(stddev), 0) - nullif(min(stddev),0)) / least(abs(nullif(min(stddev),0)), abs(nullif(max(stddev),0))))
end
as stddev_quality,
    avg(median) filter (where r = 8) as median_value,
       case
          --if value is null, no need to calculate quality
          when avg(median) filter (where r = 8) is null then null
          when (nullif(max(median),0) / nullif(min(median), 0)) > 0
          then log10(nullif(max(median), 0) / nullif(min(median),0))
          else log10((nullif(max(median), 0) - nullif(min(median),0)) / least(abs(nullif(min(median),0)), abs(nullif(max(median),0))))
end
as median_quality
from (
        select r,
    --if sum = 0 probably all values 0 and need to set null to calculate quality
		    nullif(sum(m), 0) as sum,
    --filter min != 0 values to be able to calculate quality
            min(m) filter (where m!=0),
    --filter max != 0 values to be able to calculate quality
            max(m) filter (where m!=0),
    --if avg = 0 probably all values 0 and need to set null to calculate quality
		    nullif(avg(m), 0) as mean,
    --stddev can be 0 when data evenly distributed https://www.quora.com/What-is-meant-by-standard-deviation-is-zero set null for those values to calculate quality
            nullif(stddev(m), 0) as stddev,
    --check 0's to calculate quality
		    nullif(percentile_cont(0.5) within group (order by m), 0) as median
    from (select (%s / nullif(%s,0)) as m, resolution as r from stat_area) z
    group by r
    order by r ) z