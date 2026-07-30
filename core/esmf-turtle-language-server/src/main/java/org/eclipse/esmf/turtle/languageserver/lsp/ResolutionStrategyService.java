/*
 * Copyright (c) 2026 Robert Bosch Manufacturing Solutions GmbH, Germany. All rights reserved.
 */

package org.eclipse.esmf.turtle.languageserver.lsp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.eclipse.esmf.aspectmodel.resolver.EitherStrategy;
import org.eclipse.esmf.aspectmodel.resolver.FileSystemStrategy;
import org.eclipse.esmf.aspectmodel.resolver.ResolutionStrategy;
import org.eclipse.esmf.aspectmodel.resolver.exceptions.ModelResolutionException;
import org.eclipse.esmf.aspectmodel.resolver.fs.FlatModelsRoot;
import org.eclipse.esmf.aspectmodel.resolver.fs.StructuredModelsRoot;
import org.eclipse.esmf.aspectmodel.resolver.github.GitHubStrategy;
import org.eclipse.esmf.aspectmodel.resolver.github.GithubModelSourceConfig;
import org.eclipse.esmf.turtle.languageserver.aspect.MetaModelStrategy;
import org.eclipse.esmf.turtle.languageserver.aspect.navigation.GithubMaterializingStrategy;
import org.eclipse.esmf.turtle.languageserver.lsp.config.GithubResolutionConfiguration;
import org.eclipse.esmf.turtle.languageserver.lsp.config.LspModelResolutionConfigurationParser;
import org.eclipse.esmf.turtle.languageserver.lsp.text.ParsedDocument;

import net.harawata.appdirs.AppDirsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolutionStrategyService {
   private static final Logger LOG = LoggerFactory.getLogger( ResolutionStrategyService.class );

   private static final Path PLUGIN_DIRECTORY =
         Path.of( AppDirsFactory.getInstance().getUserDataDir( "esmf", "1", "esmf" ) ).resolve( "plugins" );
   private static final ClassLoader PLUGIN_CLASS_LOADER = createPluginClassLoader( PLUGIN_DIRECTORY );

   private final GithubResolutionConfiguration githubConfiguration = new GithubResolutionConfiguration();
   private final MetaModelStrategy metaModelStrategy = new MetaModelStrategy();
   private final Map<GithubModelSourceConfig, ResolutionStrategy> githubStrategyCache = new ConcurrentHashMap<>();

   public void applyConfigurationChange( final Object settings ) {
      final List<GithubModelSourceConfig> repositories = LspModelResolutionConfigurationParser.parse( settings );
      githubConfiguration.update( repositories );
      // Drop cached strategies for repositories that are no longer configured (or whose token/branch/etc.
      // changed, which yields a different config key), so we don't keep serving stale downloads.
      githubStrategyCache.keySet().retainAll( new HashSet<>( repositories ) );
      LOG.debug( "[resolution-strategy-service] GitHub configuration updated: {} repositories configured", repositories.size() );
   }

   public ResolutionStrategy buildResolutionStrategyForDocument( final ParsedDocument parsedDocument ) {
      final Path openFilePath;
      try {
         openFilePath = Path.of( new URI( parsedDocument.getUri() ) );
      } catch ( final URISyntaxException e ) {
         throw new ModelResolutionException( "Failed to parse URI: " + parsedDocument.getUri(), e );
      }
      final FileSystemStrategy structuredStrategy = new FileSystemStrategy(
            new StructuredModelsRoot( openFilePath.getParent().getParent().getParent() ) );
      final FileSystemStrategy flatStrategy = new FileSystemStrategy( new FlatModelsRoot( openFilePath.getParent() ) );

      final List<ResolutionStrategy> strategies = new ArrayList<>();
      strategies.add( structuredStrategy );
      strategies.add( flatStrategy );
      strategies.add( metaModelStrategy );
      // Custom strategies contributed via classpath or jars dropped into PLUGINS_DIR, discovered via
      // META-INF/services/org.eclipse.esmf.aspectmodel.resolver.ResolutionStrategy
      try {
         ServiceLoader.load( ResolutionStrategy.class, PLUGIN_CLASS_LOADER ).forEach( strategy -> {
            strategies.add( strategy );
            LOG.debug( "[resolution-strategy-service] Loaded custom resolution strategy: {}", strategy.getClass().getName() );
         } );
      } catch ( final ServiceConfigurationError | LinkageError e ) {
         LOG.warn( "[resolution-strategy-service] Unable to load custom resolution strategy: {}", e.getMessage() );
      }
      for ( final GithubModelSourceConfig repository : githubConfiguration.repositories() ) {
         strategies.add( githubStrategyCache.computeIfAbsent( repository,
               config -> new GithubMaterializingStrategy( new GitHubStrategy( config ) ) ) );
      }
      return new EitherStrategy( strategies );
   }

   private static ClassLoader createPluginClassLoader( final Path pluginDirectory ) {
      ensurePluginDirectoryExists( pluginDirectory );
      try ( Stream<Path> files = Files.list( pluginDirectory ) ) {
         final URL[] jarUrls = files
               .filter( path -> path.toString().endsWith( ".jar" ) )
               .map( ResolutionStrategyService::toUrl )
               .toArray( URL[]::new );
         LOG.debug( "[resolution-strategy-service] Found {} plugin jar(s) in {}", jarUrls.length, pluginDirectory );
         return new URLClassLoader( jarUrls, ResolutionStrategyService.class.getClassLoader() );
      } catch ( final IOException exception ) {
         LOG.warn( "[resolution-strategy-service] Could not read plugins directory {}", pluginDirectory, exception );
         return ResolutionStrategyService.class.getClassLoader();
      }
   }

   private static URL toUrl( final Path path ) {
      try {
         return path.toUri().toURL();
      } catch ( final MalformedURLException exception ) {
         throw new ModelResolutionException( "Could not read plugin jar " + path, exception );
      }
   }

   private static void ensurePluginDirectoryExists( final Path pluginDirectory ) {
      if ( !Files.exists( pluginDirectory ) ) {
         try {
            Files.createDirectories( pluginDirectory );
         } catch ( final IOException exception ) {
            throw new ModelResolutionException( "Unable to create plugins directory at " + pluginDirectory, exception );
         }
      }
   }
}

