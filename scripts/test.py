from locust import HttpUser, task
from random import Random

def get_file(filename):
    with open(file=filename, mode="r") as source:
        return source.read()

class WebsiteTestUser(HttpUser):

    def on_start(self):
        """ on_start is called when a Locust start before any task is scheduled """
        pass

    def on_stop(self):
        """ on_stop is called when the TaskSet is stopping """
        pass

    @task(1)
    def index_page(self):
        file_name = "geom%s.json" % Random().randint(a=0, b=10)
        geom = get_file(file_name)
        query = """
        {
            polygonStatistic(polygonStatisticRequest:{ %s })
            {
                bivariateStatistic {
              axis{
      label,
      steps{
        label,
        value
      },
      quality,
      quotient
    },
    meta{
      min_zoom,
      max_zoom
    },
	indicators{
      name,
      label,
      direction,
      copyrights
    },
    colors{
      fallback,
      combinations{
        color,
        color_comment,
        corner
      }
    },
    initAxis{
      x{
      	label,
      	steps{
        	label,
        	value
      	},
      	quality,
      	quotient
      },
      y{
      	label,
      	steps{
        	label,
        	value
      	},
      	quality,
      	quotient
      }
    },
    overlays{
      name,
      description,
      active,
      colors{
        id,
        color
      },
      x{
      	label,
      	steps{
        	label,
        	value
      	},
      	quality,
      	quotient
      },
      y{
      	label,
      	steps{
        	label,
        	value
      	},
      	quality,
      	quotient
      }
    },
    correlationRates{
      rate,
      quality,
      correlation,
      x{
      	label,
      	steps{
        	label,
        	value
      	},
      	quality,
      	quotient
      },
      y{
      	label,
      	steps{
        	label,
        	value
      	},
      	quality,
      	quotient
      }
    },
    indicators{
      name,
      label,
      copyrights
    }
    }
            }
        }
            """ % geom

        response = self.client.post(
            "/insights-api/graphql",
            name=file_name,
            json={"query": query}
        )
