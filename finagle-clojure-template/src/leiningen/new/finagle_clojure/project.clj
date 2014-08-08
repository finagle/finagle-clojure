(defproject {{project-name}} "0.1.0-SNAPSHOT"
  :description "{{& description}}"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  {{#misc-config}}
  {{& key}} {{& value}}
  {{/misc-config}}
  :dependencies [
{{#dependencies}}
                 {{& dependency}}
{{/dependencies}}
                ])
