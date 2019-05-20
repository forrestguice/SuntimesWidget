<h1>Screenshots</h1>
<div>{% include gallery_nav.html %}</div>
<hr />
{% capture locale_list %}{% for locale in site.app_locales %}{{ locale }}|{% endfor %}{% endcapture %}
{% capture versions %}{% for version in site.app_versions %}{{ version }}|{% endfor %}{% endcapture %}
{% capture versions1 %}{% for version1 in site.app_versions1 %}{{ version1 }}|{% endfor %}{% endcapture %}
{% include gallery.html 
	screenshot_locales = locale_list
        screenshot_versions = versions
        screenshot_versions1 = versions1
	screenshot_width = '280xp' screenshot_suffix = "dark.png" %}
<hr />
<hr />
{% include donate.html %}
<hr />
