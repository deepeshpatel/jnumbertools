#!/usr/bin/env bash
# Compare the local working tree (including uncommitted changes) with an earlier commit on the SAME benchmark source.
#
# Usage:  scripts/benchmark-compare.sh [SHA]          (or edit BASE_SHA below)
#         scripts/benchmark-compare.sh [SHA] full     (full matrix instead of the quick preset; slow)
#
# The old commit is built in a temporary git worktree, so your working tree and uncommitted changes are never touched
# and no branch is switched. The current benchmark sources are copied into the worktree, so both sides run identical
# benchmark code against different production code. Result files go to benchmark-results/ (git-ignored).
#
# A short run only exposes large changes: "no regression found" does not mean "proven faster".
set -euo pipefail

BASE_SHA="${1:-PUT_SHA_HERE}"
PRESET="${2:-quick}"
BENCH_SRC="src/test/java/io/github/deepeshpatel/jnumbertools/benchmarks"
GEN="io.github.deepeshpatel.jnumbertools.benchmarks.runner.BenchmarkCommandGenerator"

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"
OUT="$ROOT/benchmark-results"
STAMP="$(date +%Y%m%d-%H%M%S)"
WT_PARENT="$(mktemp -d)"
WT="$WT_PARENT/base"
mkdir -p "$OUT"

cleanup() {
  status=$?
  cd "$ROOT"
  git worktree remove --force "$WT" >/dev/null 2>&1 || true
  rm -rf "$WT_PARENT"
  git worktree prune >/dev/null 2>&1 || true
  echo
  echo "Working tree restored: $(git rev-parse --abbrev-ref HEAD) @ $(git rev-parse --short HEAD)"
  git status -sb | head -1
  exit $status
}
trap cleanup EXIT

if [ "$BASE_SHA" = "PUT_SHA_HERE" ]; then
  echo "Give a commit: scripts/benchmark-compare.sh <SHA>" >&2
  exit 1
fi
if ! git cat-file -e "$BASE_SHA^{commit}" 2>/dev/null; then
  git fetch --quiet origin || true
  git cat-file -e "$BASE_SHA^{commit}" 2>/dev/null || { echo "Unknown commit: $BASE_SHA" >&2; exit 1; }
fi
BASE_SHORT="$(git rev-parse --short "$BASE_SHA")"

# Build one tree and run the preset there; the JMH JSON, log and environment file are copied to $OUT.
run_tree() {
  local dir="$1" label="$2"
  echo
  echo "=== $label: build and run ($dir) ==="
  (
    cd "$dir" &&
    mvn -q -DskipTests test-compile dependency:build-classpath \
        -Dmdep.includeScope=test -Dmdep.outputFile=target/test-classpath.txt &&
    CP="target/test-classes:target/classes:$(cat target/test-classpath.txt)" &&
    CMD="$(java -cp "$CP" "$GEN" --print "$PRESET" "$label-$STAMP")" &&
    echo "$CMD" &&
    bash -c "set -o pipefail; $CMD" &&
    cp benchmark-results/"$label-$STAMP".* "$OUT"/
  )
}

echo "Base commit : $BASE_SHORT ($(git log -1 --format=%s "$BASE_SHA"))"
echo "Local tree  : $(git rev-parse --short HEAD) plus $(git status --porcelain | wc -l | tr -d ' ') uncommitted file(s)"
echo "Preset      : $PRESET"

git worktree add --detach "$WT" "$BASE_SHA" >/dev/null
# Same benchmark source on both sides.
rm -rf "$WT/$BENCH_SRC"
cp -R "$ROOT/$BENCH_SRC" "$WT/$BENCH_SRC"

if ! run_tree "$WT" "base-$BASE_SHORT"; then
  echo >&2
  echo "The benchmark could not be built or run at $BASE_SHORT (an API the benchmark uses may not exist there)." >&2
  echo "Pick a newer base commit, or compare only the implementations that exist at both." >&2
  exit 1
fi
run_tree "$ROOT" "local" || { echo "The local build or benchmark run failed." >&2; exit 1; }

echo
echo "=== Comparison ==="
java "$ROOT/scripts/BenchmarkReport.java" compare \
  "$OUT/base-$BASE_SHORT-$STAMP.json" "$OUT/local-$STAMP.json" "$BASE_SHORT" "local" \
  | tee "$OUT/compare-$BASE_SHORT-$STAMP.txt"
