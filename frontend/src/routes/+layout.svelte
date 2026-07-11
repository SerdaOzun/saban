<script lang="ts">
  import "./layout.css";
  import MainNavbar from "../components/MainNavbar.svelte";
  import SabanFooter from "../components/footer/SabanFooter.svelte";

  let { children } = $props();
</script>

<div class="min-h-screen flex flex-col gap-0">
  <MainNavbar />
  <main class="flex-grow pt-16">
    {@render children()}
  </main>
  <SabanFooter />
</div>
