(ns narjure.bag
  (:require [avl.clj :as avl]))

(defn compare-elements
  "Compares elements. If priority of elements is equal, compares the hashes of
  the ids. It is done to allow bag to contain elements with equal priority."
  [{p1 :priority id1 :id}
   {p2 :priority id2 :id}]
  (if (= p1 p2)
    (> (hash id1) (hash id2))
    (and (> p1 p2) (not= id1 id2))))

(defprotocol Bag
  (add-element
    ;Adds element to bag, removes element with lowest priority if bag is full.
    [_ element])
  (get-by-index
    ;Returns tuple of element and updated bag. If element with such index
    ; doesn't exist returns tuple of nil and bag without any changes.
    [_ index])
  (get-by-id
    ;Returns tuple of element and updated bag. If element with such id
    ; doesn't exist returns tuple of nil and bag without any changes.
    [_ id])
  (pop-element
    ;Returns tuple of element and updated bag. If bag is empty returns tuple
    ; of nil and bag without any changes.
    [_])
  (update-element
    ;Replaces entrie with the same id.
    [_ element])
  (count-elements [_]))

(defrecord DefaultBag [priority-index elements-map capacity]
  Bag
  (add-element [bag element]
    (let [cnt (count priority-index)]
      (if (>= cnt capacity)
        (let [[_ bag'] (pop-element bag)]
          (add-element bag' element))
        (let [{:keys [id priority]} element
              priority-index' (conj priority-index {:id       id
                                                    :priority priority})
              element-map' (assoc elements-map id element)]
          (->DefaultBag priority-index' element-map' capacity)))))

  (get-by-index [bag index]
    (if (< index (count priority-index))
      (let [{:keys [id] :as element} (nth priority-index index)
            priority-index' (disj priority-index element)
            element' (elements-map id)
            element-map' (dissoc elements-map id)]
        [element' (->DefaultBag priority-index' element-map' capacity)])
      [nil bag]))

  (get-by-id [bag id]
    (if-let [{:keys [priority] :as element'} (elements-map id)]
      (let [priority-index' (disj priority-index {:id       id
                                                  :priority priority})
            element-map' (dissoc elements-map id)]
        [element' (->DefaultBag priority-index' element-map' capacity)])
      [nil bag]))

  (pop-element [bag]
    (let [cnt (count priority-index)]
      (if (pos? cnt)
        (let [{:keys [id] :as element} (nth priority-index (dec cnt))
              priority-index' (disj priority-index element)
              element' (elements-map id)
              elements-map' (dissoc elements-map id)]
          [element' (->DefaultBag priority-index' elements-map' capacity)])
        [nil bag])))

  (update-element [bag element]
    (let [id (element :id)
          [_ bag'] (get-by-id bag id)]
      (add-element bag' element)))

  (count-elements [_] (count priority-index)))

(defn default-bag
  ([] (default-bag 50))
  ([capacity]
   (->DefaultBag (avl/sorted-set-by compare-elements) {} capacity)))
