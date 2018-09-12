### Tiles

![ex](https://static-avnbmbcpey.now.sh/img/Screen%20Shot%202018-09-11%20at%2012.26.48%20AM.png)

Drawing with blocks, but mostly an exercise to try some technical things out.

This app is a quick example of how to build a stateless UI, with a local app db that's queryable using GraphQL. The app uses Artemis for data normalization, and GraphQL doc creation / parsing. In the future, this can be transitioned to a graphql backend and the view components will not need to alter their queries.

Another crucial part to this stack along side the central source of truth is the ability for components, no matter their position in the view tree, to query for any data they might need. This makes scaling out large UI's a lot easier. When bugs crop up, being able to local the view with the bug and have the whole world their saves a lot of time. While passing props seems harmless and functional on the surface, you're coupling components to their parents. Often times if you're looking at a component's `data` prop, who knows where that data came from, was it munged? It's better to do the work up front to empower components with the ability to query for their needs.

Because it's stateless,  undo/redo capability has been implemented for free. Clojure's persistent data structures make copying the DB on change inexpensive.
