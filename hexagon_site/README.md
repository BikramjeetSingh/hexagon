
# Project Site

To generate the site source and run it inside a server, execute: `gw serveSite`. The site will be
served at: [http://localhost:8000](http://localhost:8000).

The site uses [Google Analytics] for usage statistics.

The site properties are loaded from `mkdocs.yaml` file. The content from `pages` is copied to
`content` in order to add the API Markdown to content.

As `content` directory is created from `pages` processed content, it should be ignored by SCM and it
will be deleted by the clean task.

The site takes the Dokka API generated by all modules and add it to the site.

Pages have a header with a button to edit the content in [Github] and use [Disqus] to add comments
to each of them.

[Google Analytics]: https://analytics.google.com
[Github]: https://github.com
[Disqus]: https://disqus.com

# MkDocs

The MkDocs theme used is [MkDocs Material], in order to customize it, these steps were done:

1. Clone MkDocs Material
2. Change `application-palette.scss`
3. Build the theme
4. Copy theme's `css` folder to site `mkdocs_palette.css` file
5. Copy `palette.html` partial and update the indigo color in that file for the desired one
6. To generate the site use: `./gradlew buildSite` which executes:
   `docker-compose run site build -csq`. You can check more details [here][site build].

[site build]: https://hub.docker.com/r/squidfunk/mkdocs-material
[MkDocs Material]: https://squidfunk.github.io/mkdocs-material