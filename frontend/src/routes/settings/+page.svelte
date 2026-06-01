<script lang="ts">
  import Separator from "@/components/ui/separator/separator.svelte";
  import { Button } from "@/components/ui/button";
  import UserSettings from "./UserSettings.svelte";

  const navItems = [
    {
      title: "User",
    },
    {
      title: "Languages",
    },
  ];

  let selectedMenu: string = $state(navItems[0].title);
</script>

<div class="flex flex-row m-2">
  <div class="flex flex-col gap-2 mr-4 border-r-2">
    {#each navItems as navItem}
      <Button
        variant="ghost"
        onclick={() => {
          selectedMenu = navItem.title;
        }}
      >
        {navItem.title}
      </Button>
    {/each}
  </div>
  <div class="flex flex-col gap-2">
    {#if selectedMenu === "User"}
      <UserSettings />
    {:else if selectedMenu === "Languages"}
      Nothing yet
    {/if}
  </div>
</div>
