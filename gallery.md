<div id="screenshot_nav">
Suntimes is available in the following languages:<br/>
{% assign screenshot_version = 'v0.9.5' %}
{% assign screenshot_version1 = 'v0.3.1' %}
{% assign screenshot_suffix = 'dark.png' %}
{% assign screenshot_width = '280px' %}
| {% for locale in site.app_locales %}
    <a href="#{{ locale }}">{{ locale | slice: 0, 2 }}</a> |
{% endfor %}
</div>

<div id="gallery">
{% for locale in site.app_locales %}
    {% assign sectionID = locale %}
    {% assign screenshot_alt = 'screenshot-' | append: locale %}

    <div id="{{ sectionID }}">
    <a name="#{{sectionID}}" /><h2>{{ locale }} [{{ screenshot_version }}, {{ screenshot_version1}}]</h2>

    {% assign screenshot_path = 'doc/screenshots/' | append: screenshot_version | append: '/' | append: locale %}
    {% for image in site.static_files %}
        {% if image.path contains screenshot_path and image.path contains screenshot_suffix %}
            <a href="{{ site.baseurl }}{{ image.path }}"><img src="{{ site.baseurl }}{{ image.path }}" alt="{{ screenshot_alt }}" width="{{ screenshot_width }}" /></a>
        {% endif %}
    {% endfor %}

    {% assign screenshot_path1 = 'doc/screenshots/suntimescalendars/' | append: screenshot_version1 | append: '/' | append: locale %}
    {% for image in site.static_files %}
        {% if image.path contains screenshot_path1 %}
            <a href="{{ site.baseurl }}{{ image.path }}"><img src="{{ site.baseurl }}{{ image.path }}" alt="{{ screenshot_alt }}" width="{{ screenshot_width }}" /></a>&nbsp;&nbsp;
        {% endif %}
    {% endfor %}

    </div>
{% endfor %}
</div>

<hr />
<hr />
{% include_relative donate.md %}
<hr />
