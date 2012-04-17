(ns porklock.pathing
  (:use [clojure.set] 
        [porklock.fileops]
        [porklock.system])
  (:require [clojure.string :as string]
            [clojure-commons.file-utils :as ft]))

(defn exclude-files
  "Splits up the exclude option and turns them all into absolute paths."
  [{excludes :exclude delimiter :exclude-delimiter}]
  (if-not (string/blank? excludes) 
    (absify (string/split excludes (re-pattern delimiter)))
    []))

(defn include-files
  "Splits up the include option and turns them all into absolute paths."
  [{includes :include delimiter :include-delimiter}]
  (if-not (string/blank? includes) 
    (absify (string/split includes (re-pattern delimiter)))
    []))

(defn files-to-transfer
  "Constructs a list of the files that need to be transferred."
  [options]
  (let [includes (set (include-files options))
        excludes (set (exclude-files options))
        allfiles (set (files-and-dirs (:source options)))]
    (println (str "includes: " includes))
    (println (str "excludes: " excludes))
    (seq (union (difference allfiles excludes) includes))))

(defn- str-contains?
  [s match]
  (if (not= (.indexOf s match) -1)
    true
    false))

(defn- fix-path
  [transfer-file sdir ddir]
  (ft/rm-last-slash (ft/path-join ddir (string/replace transfer-file (re-pattern sdir) ""))))

(defn relative-dest-paths
  "Constructs a list of absolute destination paths based on the
   input and the given source directory."
  [transfer-files source-dir dest-dir]
  
  (let [sdir (ft/add-trailing-slash source-dir)]
    (apply merge (map
                  #(if (str-contains? %1 sdir)
                     {%1 (fix-path %1 sdir dest-dir)} 
                     {%1 %1})
                  transfer-files))))

(defn user-irods-dir
  "Returns the full path to the user's .irods directory."
  []
  (ft/path-join (user-home) ".irods"))

(defn irods-auth-filepath
  "Returns the path where the .irodsA should be."
  []
  (ft/path-join (user-irods-dir) ".irodsA"))

(defn irods-env-filepath
  "Returns the path to where the .irodsEnv file should be."
  []
  (ft/path-join (user-irods-dir) ".irodsEnv"))

(defn imkdir-path
  "Returns the path to the imkdir executable or an empty
   string if imkdir couldn't be found."
  []
  (find-file-in-path "imkdir"))

(defn iput-path
  "Returns the path to the iput executable or an empty
   string if iput couldn't be found."
  []
  (find-file-in-path "iput"))

(defn iget-path
  "Returns the path to the iget executable, or an empty
   string if iget cannot be found."
  []
  (find-file-in-path "iget"))

(defn ils-path
  "Returns the path to the ils executable or an empty
   string if ils couldn't be found."
  []
  (find-file-in-path "ils"))