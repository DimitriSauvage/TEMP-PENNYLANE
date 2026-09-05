/** Props for the search filters block. */
export interface SearchFiltersProps {
  /** Whether the "Ready to cook only" toggle is on. */
  readyToCookOnly: boolean;
}

/** Emits of the search filters block. */
export interface SearchFiltersEmits {
  /** New value of the "Ready to cook only" toggle. */
  (e: "update:readyToCookOnly", value: boolean): void;
}
